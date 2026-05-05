package top.stillmisty.xiantao.domain.fudi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public sealed interface CellConfig {

  record EmptyConfig() implements CellConfig {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record FarmConfig(
      @JsonProperty("crop_id") Integer cropId,
      @JsonProperty("plant_time") LocalDateTime plantTime,
      @JsonProperty("mature_time") LocalDateTime matureTime,
      @JsonProperty("harvest_count") int harvestCount)
      implements CellConfig {

    public FarmConfig withHarvestCount(int harvestCount) {
      return new FarmConfig(cropId, plantTime, matureTime, harvestCount);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record PenConfig(
      @JsonProperty("beast_id") Long beastId,
      @JsonProperty("template_id") Integer templateId,
      @JsonProperty("hatch_time") LocalDateTime hatchTime,
      @JsonProperty("mature_time") LocalDateTime matureTime,
      @JsonProperty("production_stored")
          @JsonDeserialize(using = ProductionStoredDeserializer.class)
          List<ProductionItem> productionStored,
      @JsonProperty("last_production_time") LocalDateTime lastProductionTime)
      implements CellConfig {

    public PenConfig {
      if (productionStored == null) productionStored = new ArrayList<>();
    }

    public PenConfig withProductionStored(List<ProductionItem> productionStored) {
      return new PenConfig(
          beastId, templateId, hatchTime, matureTime, productionStored, lastProductionTime);
    }

    public PenConfig withLastProductionTime(LocalDateTime lastProductionTime) {
      return new PenConfig(
          beastId, templateId, hatchTime, matureTime, productionStored, lastProductionTime);
    }

    public int totalProductionQuantity() {
      return productionStored.stream().mapToInt(ProductionItem::quantity).sum();
    }

    public void addProductionItem(Long templateId, String name, int quantity) {
      for (ProductionItem item : productionStored) {
        if (item.templateId().equals(templateId)) {
          int idx = productionStored.indexOf(item);
          productionStored.set(
              idx, new ProductionItem(templateId, name, item.quantity() + quantity));
          return;
        }
      }
      productionStored.add(new ProductionItem(templateId, name, quantity));
    }

    public PenConfig withClearedProduction() {
      return new PenConfig(
          beastId, templateId, hatchTime, matureTime, new ArrayList<>(), lastProductionTime);
    }
  }

  record ProductionItem(
      @JsonProperty("template_id") Long templateId,
      @JsonProperty("name") String name,
      @JsonProperty("quantity") int quantity) {}

  class ProductionStoredDeserializer extends JsonDeserializer<List<ProductionItem>> {
    @Override
    public List<ProductionItem> deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.isExpectedStartArrayToken()) {
        return ctx.readValue(
            p, ctx.getTypeFactory().constructCollectionLikeType(List.class, ProductionItem.class));
      }
      // legacy Integer format — skip and return empty list
      p.skipChildren();
      return new ArrayList<>();
    }
  }
}
