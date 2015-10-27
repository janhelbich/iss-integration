package cz.cvut.fel.iss.integration;

import cz.cvut.fel.iss.integration.model.bo.RESTResponse;
import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.dto.ObjednavkaDTO;
import cz.cvut.fel.iss.integration.model.exceptions.InvalidObjednavkaDataFormat;
import cz.cvut.fel.iss.integration.service.LocalStockService;
import cz.cvut.fel.iss.integration.service.ObjednavkaService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.ArrayList;
import java.util.List;

/**
 * Objednavka Endpoint
 */
@Startup
@Singleton
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        //
        //REST endpoint
        //
        restConfiguration()
                .component("restlet")
                .bindingMode(RestBindingMode.json_xml)
                .port(8080)

                .dataFormatProperty("prettyPrint", "true")
                .dataFormatProperty("include", "NON_NULL")
                .dataFormatProperty("json.in.disableFeatures", "FAIL_ON_UNKNOWN_PROPERTIES");


        //
        //JSON endpoint
        //
        rest("/ordersJSON").consumes("application/json").produces("application/json")
                .post().type(ObjednavkaDTO.class).outType(RESTResponse.class).to("direct:objednavka-process");


        //
        //SOAP endpoint
        //
        rest("/ordersSOAP").consumes("application/soap").produces("application/soap")
                .post().type(ObjednavkaDTO.class).outType(RESTResponse.class).to("direct:obj-preprocessSOAP");


        //
        //ROUTY
        //

        from("direct:obj-preprocessSOAP")
                .unmarshal().soapjaxb().setHeader("inputFormat", simple("SOAP")).to("direct:objednavka-process");


        //Zpracovani objednavky
        //
        from("direct:objednavka-process")
                .onException(InvalidObjednavkaDataFormat.class).handled(true)
                    .log("Invalid INPUT format detected!")
                    .to("direct:bad-request")
                .end()
                .setProperty("objednavka", body())
                .setHeader("objednavkaIn", body()) // zaloha vstupu
                .bean(ObjednavkaService.class, "isValid") //je vstup validni?
                .bean(ObjednavkaService.class, "create") //prevod na BO
                .to("direct:new-objednavka");


        //Spatna data
        from("direct:bad-request")
                .setProperty("description", simple("${body}"))
                //.bean(ResponseBuilder.class, "create")
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400)); // BAD REQUEST


        //Zpracovani polozek
        //
        from("direct:new-objednavka")
                //.transacted()
                .split(simple("${body.wantedItems}").resultType(ItemBO.class), new AggregationStrategy() {
                    @Override
                    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                        List<ItemBO> list = null;
                        ItemBO newItem = newExchange.getIn().getBody(ItemBO.class);
                        if (oldExchange == null) {
                            list = new ArrayList<>();
                            list.add(newItem);
                            newExchange.getIn().setBody(list);
                            return newExchange;
                        }
                        list = oldExchange.getIn().getBody(ArrayList.class);
                        list.add(newItem);
                        return oldExchange;
                    }
                })
                    .log(">>Trying to process>>" + String.valueOf(simple("${body}")))
                    .inOut("direct:item-process")
                .end()
                .log(">>SPLITTED >>" + String.valueOf(simple("${body}")))
                .setHeader("orderItems", simple("${body}"))
                //.bean(UserService.class, "checkForVIPStatus") //TODO dodelat metodu boolean checkForVIPStatus(Objednavka obj);
                .choice()
                    .when(simple("${body} == true and ${header:VIP} == true")).log("VIP objednavka obdrzena")
                        .to("direct:accounting-insertion")
                    .when(simple("${body} == false")).log("Standardni objednavka obdrzena")
                        .to("direct:accounting-insertion")
                    .otherwise().log("NonVIP customer VIP order attempt")
                .end()
                .log(String.valueOf(simple("${body}"))); // temp
                //.setBody(constant(null));


        //Zpracovani Itemu
        //TODO
        from("direct:item-process").log(">>Item Processing>>" + String.valueOf(simple("${body}")))
                .setProperty("item", simple("${body}"))
                .setHeader("POM",simple("${body}"))
                .bean(LocalStockService.class, "isInStock")
                .setBody(header("POM")).removeHeader("POM")
                .choice()
                    .when(simple("${body} == true")).log(">>Item available locally") //.when(simple("${body.stock} == LOCAL"))
                    .otherwise().log(">>Need to look to suppliers")//.to("direct:item-suppliers-availability")
                .end();


        from("direct:item-local-availability").log("checking local availability");
//                .transacted()
//                .bean(ObjednavkaService.class,"isAvailableLocal");

        from("direct:item-suppliers-availability").log("checking Suppliers availability");
//                .setHeader("localItem", simple("${body}"))
//                //.transacted()
//
//                //Dotazovani dodavatelu
//                .inOut("activemq:supplierA:available") //TODO volani supplierA
//                .setProperty("supplierAItem", simple("${body}"))
//                .setBody(constant(null))
//                .inOut("activemq:supplierB:available") //TODO volani supplierB
//                .setProperty("supplierBItem", simple("${body}"))
//                .setBody(constant(null))
//
//                //Vyber nejlevnejsi ceny
//                .bean(ObjednavkaService.class, "selectCheaperItem")
//                .setProperty("rightItem", simple("${body}"))
//                //Porovnani s lokalni cenou
//                .bean(LocalStockService.class, "isPriceHigherThanLocal")
//                .choice()
//                    .when(simple("${body} == true and ${header:VIP} == true")).setBody(simple("${property:rightItem}"))
//                    .otherwise().setBody(constant(null))
//                .end();


        //
        //Vystaveni faktury v ucetnictvi
        //
        from("direct:accounting-insertion").log("Accounting insertion...");
//            .to(ExchangePattern.InOut, "https://localhost:8443/accounting/rest/accounting/invoice/issue") //TODO POST method
//            .choice()
//                .when(simple("${body.status} == INVALID and ${body.invoiceId} == -1")) //TODO routovat nekam chybu
//                .otherwise().log("Fakutra vlozena")
//        .end();


        //
        //Expedice, export zbozi
        //
//        from("direct:expedition")
//                //.transacted()
//
//                //.split() //rozsekat na itemy a odecitat ze skladu
//
//                .choice()
//                    .when(header("VIP"))
//                .end()
//                .log();

        //
        //Odpoved
        //
//        from("direct:createResponse")
//                .bean(ResponseBuilder.class, "generateNewResponse")
//                .setProperty("outputFormat", simple("${header:inputFormat}"))
//                .bean(ResponseBuilder.class, "getResponse")
//                .log("Response generated" + String.valueOf(simple("${property:outputFormat}")));

    }

}
