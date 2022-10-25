package com.petrushin.springmvc2.sheets;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Component;
import org.w3c.dom.ranges.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class MyGoogleSheets {
    private final String APPLICATION_NAME = "Sheets";
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final String TOKENS_DIRECTORY_PATH = "tokens";

    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private Sheets service;
    private final String spreadsheetId = "1gUhnlO8BG9z5M5HCKJyRLSRtcyYqgUZ3BCopSO1zrIk";


    //в теории создает одно подключение которое будем использовать пока программа запущена
    public MyGoogleSheets(){
        makeConnection();
    }


    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = MyGoogleSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private void makeConnection(){
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public List<List<Object>> getValues(String range){
        try {

            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            return values;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    public void writeValues(String range, String info){
        try {

            List<List<Object>> list = new ArrayList<>();
            List<Object> strings = Arrays.asList(info.split("/-/"));
            list.add(strings);
            ValueRange body = new ValueRange().setValues(list);
            AppendValuesResponse result = service.spreadsheets().values()
                    .append(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public void updateValues(String range, String oldInfo, String newInfo){
        try {
            List<List<Object>> values = getValues(range);
            List<Object> oldListObj = Arrays.asList(oldInfo.split("/-/"));
            List<Object> newListObj = Arrays.asList(newInfo.split("/-/"));
            ValueRange body = new ValueRange().setValues(Arrays.asList(newListObj));
            //для нахождения нужной строки
            int counter = 0;
            for(List row :  values){
                counter++;
                if(row.equals(oldListObj)) {
                    UpdateValuesResponse result = service.spreadsheets().values()
                            .update(spreadsheetId, range + "!" + counter + ":" + counter, new ValueRange().setValues(Arrays.asList(newListObj)))
                            .setValueInputOption("USER_ENTERED")
                            .execute();
                    return;
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteRows(String range, String info, int sheetId){
        List<List<Object>> values = getValues(range);
        List<Object> list = Arrays.asList(info.split("/-/"));
        int counter = -1;
        for (List row : values){
            counter++;
            if (row.get(0).equals(list.get(0))){
                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                                                            .setRange(
                                                                    new DimensionRange()
                                                                    .setSheetId(sheetId)
                                                                    .setDimension("ROWS")
                                                                    .setStartIndex(counter)
                                                                    .setEndIndex(counter + 1)
                                                            );
                List<Request> requests = new ArrayList<>();
                requests.add(new Request().setDeleteDimension(deleteRequest));
                BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
                try {
                    service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }
}
