package org.folio.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

public class ResponseHelper {

  private ResponseHelper() {
  }

  public static Response statusWithText(int status, String msg) {
    return Response.status(status)
      .type(MediaType.TEXT_PLAIN)
      .entity(StringUtils.defaultString(msg))
      .build();
  }

  public static Response statusWithJson(int status, Object json) {
    return Response.status(status)
      .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON)
      .entity(json)
      .build();
  }

}
