package me.name.bot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class SheetHandler {
    private final String APPLICATION_NAME = "Stand In Fire DPS Higher Game Finder";
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final String TOKENS_DIRECTORY_PATH = "tokens";
    private final NetHttpTransport HTTP_TRANSPORT;
    private final String spreadsheetId;
    private final String range;
    private Sheets service;
    private String[] gamerTags_respons;
    private String[] gamerTags;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public SheetHandler() throws IOException, GeneralSecurityException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        spreadsheetId = "Spreadsheet ID here";
        range = "B2:S";
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String analyseSheet(String[] gamerTags) throws IOException {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String analysis = "";
        this.gamerTags = gamerTags;
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            analysis = "Google Sheets data ikke funnet";
        } else {
            for (int i = 0; i < values.size(); i++) {
                List row = values.get(i);
                // first row with gamerTags
                if (i == 0) {
                    row.remove(0);
                    gamerTags_respons = Arrays.copyOf(row.toArray(), row.toArray().length, String[].class);
                // rest of rows
                } else {
                    String game = (String)row.get(0);
                    row.remove(0);
                    int rating = 0;
                    for (int j = 0; j < gamerTags_respons.length+1; j++) {
                        try {
                            if (thisCount(j) && ((String)row.get(j)).equals("✓")) { rating++; }
                        } catch (IndexOutOfBoundsException e) {}
                    }
                    map.put(game, rating);
                }
            }
            ValueComparator bvc =  new ValueComparator(map);
            TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
            sorted_map.putAll(map);
            int top10 = 0;
            for(Map.Entry<String,Integer> entry : sorted_map.entrySet()) {
                analysis += entry.getValue()+"\t"+entry.getKey()+"\n";
                if (top10>18) break;
                top10++;
            }
        }
        return analysis;
    }

    private boolean thisCount(int index) {
        for (String tag : gamerTags) {
            if (gamerTags_respons[index].equals(tag)) return true;
        }
        return false;
    }
}

class ValueComparator implements Comparator<String> {

    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}