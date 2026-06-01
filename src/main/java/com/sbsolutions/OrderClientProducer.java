package com.sbsolutions;

import com.sbsolutions.api.DonutsClient;
import com.sbsolutions.api.PricingSheetClient;
import com.sbsolutions.api.RollClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class OrderClientProducer {

  @ConfigProperty(name = "ORDER_DATA_REST_URL", defaultValue = "https://order-data.fly.dev")
  String orderDataRestUrl;

  @Produces
  @ApplicationScoped
  public DonutsClient donutsClient() {
    return new DonutsClient(orderDataRestUrl);
  }

  @Produces
  @ApplicationScoped
  public RollClient rollClient() {
    return new RollClient(orderDataRestUrl);
  }

  @Produces
  @ApplicationScoped
  public PricingSheetClient pricingSheetClient() {
    return new PricingSheetClient(orderDataRestUrl);
  }
}
