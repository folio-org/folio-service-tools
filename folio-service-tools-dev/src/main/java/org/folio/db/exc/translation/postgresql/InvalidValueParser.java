package org.folio.db.exc.translation.postgresql;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.IterableGet;
import org.apache.commons.collections4.map.HashedMap;

class InvalidValueParser {

  private static final HashedMap<String, String> EMPTY_MAP = new HashedMap<>(0);

  // Sample message to be parsed:
  //  Key (parent_id1, parent_id2)=(22222, 813205855) is not present in table
  //
  // It always starts with "Key" and contains the data in form of
  //    (<field-name1>, .. , <field-nameN>)=(<value1>, .. , <valueN>)
  private static final Pattern VALUE_PATTERN =
    Pattern.compile("\\s*Key\\s*\\((.+)\\)\\s*=\\s*\\((.+)\\).+");

  private String details;


  InvalidValueParser(ErrorMessageAdapter messageAdapter) {
    this.details = messageAdapter.getDetailedMessage().orElse(null);
  }

  IterableGet<String, String> parse() {
    if (isBlank(details)) {
      return EMPTY_MAP;
    }

    HashedMap<String, String> result = new HashedMap<>();

    Matcher matcher = VALUE_PATTERN.matcher(details);
    if (matcher.matches()) {
      String[] keys = matcher.group(1).split(",");
      String[] values = matcher.group(2).split(",");

      if (keys.length == values.length) {
        for (int i = 0; i < keys.length; i++) {
          String key = keys[i].trim();
          String value = values[i].trim();

          result.put(key, value);
        }
      }
    }

    return result;
  }

}
