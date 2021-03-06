package com.mmgo.cashlessmap.entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.grpc.internal.IoUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public class GoogleMapApiClient {

    private Gson gson = new Gson();

    private final String DIRECTIONS_API_HOST = "https://maps.googleapis.com/maps/api/directions/json";
    private final String KEY = "AIzaSyD3as-pI0qLXEte93YpO0MQ7cISB0jrf6Q";

    public GoogleMapApiResponse execute(List<Coordinate> coordinates) {
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(getDirection(coordinates));) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return transformFrom(parseText(response.getEntity()));
            } else {
                return this.processNotFoundResult(parseText(response.getEntity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpUriRequest getDirection(List<Coordinate> coordinates) {
        URIBuilder builder = null;
        try {
            builder = new URIBuilder(DIRECTIONS_API_HOST);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        builder.setParameter("origin", coordinates.get(0).toString());
        builder.setParameter("destination", coordinates.get(1).toString());
        builder.setParameter("mode", "walking");
        builder.setParameter("key", KEY);
        try {
            return new HttpGet(builder.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseText(HttpEntity entity) throws JsonSyntaxException, ParseException, IOException {
        String response;
        InputStream inputStream = entity.getContent();
        byte[] bytes = IoUtils.toByteArray(inputStream);
        response = new String(bytes, "UTF-8");
        return response;
    }

    private GoogleMapApiResponse transformFrom(String response) {
        JsonObject object = gson.fromJson(response, JsonObject.class);
        GoogleMapApiResponse googleMapApiResponse = new GoogleMapApiResponse();

        JsonArray routes = object.get("routes").getAsJsonArray();
        googleMapApiResponse.duration = routes.get(0).getAsJsonObject().get("legs").getAsJsonArray().get(0).getAsJsonObject().get("duration").getAsJsonObject().get("text").getAsString();

        return googleMapApiResponse;
    }

    private GoogleMapApiResponse processNotFoundResult(String response) {
        return new GoogleMapApiResponse();
    }
}