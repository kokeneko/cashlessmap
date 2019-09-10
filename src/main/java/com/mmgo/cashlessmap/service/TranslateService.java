package com.mmgo.cashlessmap.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mmgo.cashlessmap.entity.Translate;


import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TranslateService {

    
    private String credential =  "";

    @Autowired
    private Gson gson;

    public String translate(Translate translate) throws JsonSyntaxException, ParseException, IOException, HttpException {
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(createQueryHttpPost(translate));) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return parseTranslationText(response.getEntity());
            } else {
                throw new HttpException(Integer.toString(statusCode));
            }
        }
    }

    private HttpPost createQueryHttpPost(Translate translate) {
    	URIBuilder builder = null;
		try {
			builder = new URIBuilder("https://translation.googleapis.com/language/translate/v2");
		} catch (URISyntaxException e) {	
			e.printStackTrace();
		}
		builder.setParameter("q", translate.getText()).setParameter("target", translate.getTargetLanguage()).setParameter("key",credential);
    try {
			return new HttpPost(builder.build());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    private String parseTranslationText(HttpEntity entity) throws JsonSyntaxException, ParseException, IOException {
      JsonObject object = gson.fromJson(EntityUtils.toString(entity, "UTF-8"), JsonObject.class);
      String result = object.getAsJsonObject("data")
          .getAsJsonArray("translations")
          .get(0)
          .getAsJsonObject()
          .get("translatedText")
          .getAsString();
      return result;
    }

}