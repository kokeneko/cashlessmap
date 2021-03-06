package com.mmgo.cashlessmap.entity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import com.google.gson.JsonElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mmgo.cashlessmap.utility.Option;

import io.grpc.internal.IoUtils;
import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;

public class GurunaviApiClient {

	private Gson gson = new Gson();
	
	private final String CREDENTIAL = "a067136c683743ad04c1e0c7c48a7af4"; // TODO property
	private final String GNAVI_API_HOST = "https://api.gnavi.co.jp/RestSearchAPI/v3/";
	
	public Stores execute(Option option) throws JsonSyntaxException, ParseException, IOException {
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(findStore(option));) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return transformFrom(parseText(response.getEntity()));
            } else {
	            return this.processNotFoundResult(parseText(response.getEntity()));
            }
        }
    }
	
	private HttpGet findStore(Option option) {
    	URIBuilder builder = null;
		try {
			builder = new URIBuilder(GNAVI_API_HOST);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if (StringUtils.isEmpty(option.storeId) 
		&& StringUtils.isNotEmpty(option.getFreeWord())) {
			builder.setParameter("keyid", CREDENTIAL)
			.setParameter("freeword", option.getFreeWord());

		} else if (StringUtils.isEmpty(option.storeId)) {
			builder.setParameter("keyid", CREDENTIAL)
			.setParameter("latitude", option.latitude)
			.setParameter("longitude",option.longitude)
			.setParameter("e_money",option.eMoney.toString())
			.setParameter("lunch",option.lunch.toString())
			.setParameter("no_smoking",option.noSmoking.toString())
			.setParameter("bottomless_cup", option.bottomlessCup.toString())
			.setParameter("private_room", option.privateRoom.toString())
			.setParameter("wifi", option.wifi.toString())
			.setParameter("card", option.card.toString())
			.setParameter("hit_per_page", option.hitPerPage.toString())
			.setParameter("range",option.range)
			.setParameter("freeword",option.translatedSeachText);
			
		}  else {
			builder.setParameter("keyid", CREDENTIAL)
			.setParameter("id", option.storeId);
		}
		
    	try {
			return new HttpGet(builder.build());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
	private String parseText(HttpEntity entity) throws JsonSyntaxException, ParseException, IOException {
		String response;
		InputStream istream = entity.getContent();
		byte[] bytes = IoUtils.toByteArray(istream);
		response = new String(bytes, "UTF-8");
		return response;
    }
	
	private Stores transformFrom(String response) {
		JsonObject object = gson.fromJson(response, JsonObject.class);
		Stores stores = new Stores();

		for(JsonElement element : object.getAsJsonArray("rest")) {
            Store store = new Store();
            store.name = element.getAsJsonObject().get("name").getAsString();
            store.storeId = element.getAsJsonObject().get("id").getAsString();
            store.prShort = element.getAsJsonObject().get("pr").getAsJsonObject().get("pr_short").getAsString();
            store.prLong = element.getAsJsonObject().get("pr").getAsJsonObject().get("pr_long").getAsString();
            store.creditCard = element.getAsJsonObject().get("credit_card").getAsString();
            store.eMoney = element.getAsJsonObject().get("e_money").getAsString();
            store.tel = element.getAsJsonObject().get("tel").getAsString();
            store.shopImage1 = element.getAsJsonObject().get("image_url").getAsJsonObject().get("shop_image1").getAsString();
            store.shopImage2 = element.getAsJsonObject().get("image_url").getAsJsonObject().get("shop_image2").getAsString();
            store.qrCode = element.getAsJsonObject().get("image_url").getAsJsonObject().get("qrcode").getAsString();
            store.latitude = element.getAsJsonObject().get("latitude").getAsString();
            store.longitude = element.getAsJsonObject().get("longitude").getAsString();
            stores.add(store);
		}
        return stores;
	}
	
	private Stores processNotFoundResult(String response) {
        return new Stores();
	}
}
