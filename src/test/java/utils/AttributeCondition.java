package utils;

import java.util.Map;
import lombok.Getter;

@Getter
public class AttributeCondition {
  private final Map<String, Boolean> attributes;

  public AttributeCondition(Map<String, Boolean> attributes) {
    this.attributes = attributes;
  }

}
