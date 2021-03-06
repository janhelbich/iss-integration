package cz.cvut.fel.iss.integration;

import cz.cvut.fel.iss.integration.model.CheapestItemPickerProcessor;
import cz.cvut.fel.iss.integration.model.ObjednavkaVIPCheckProcessor;
import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.bo.OutputResponse;
import cz.cvut.fel.iss.integration.model.dto.ObjednavkaDTO;
import cz.cvut.fel.iss.integration.model.exceptions.InvalidObjednavkaDataFormat;
import cz.cvut.fel.iss.integration.service.LocalStockService;
import cz.cvut.fel.iss.integration.service.ObjednavkaService;
import cz.cvut.fel.iss.integration.service.ResponseBuilder;
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

    final static String LOCAL_STOCK_URL = "jdbc:h2:tcp://localhost/~/exam"; //creds sa/sa
    final static String SUPPLIER_A_URL = "cxf://http://localhost:8080/supplier-a/SupplierAService?"
            + "wsdlURL=http://localhost:8080/supplier-a/SupplierAService?wsdl"
            + "&serviceName=SupplierAService"
            + "&portName=SupplierAPort"
            +"&dataFormat=MESSAGE"
            +"&username=webuser&password=JBoss.123";
    final static String SUPPLIER_B_URL = "cxf://http://localhost:8080/supplier-b/SupplierBService?"
            + "wsdlURL=http://localhost:8080/supplier-b/SupplierBService?wsdl"
            + "&serviceName=SupplierBService"
            + "&portName=SupplierBPort"
            +"&dataFormat=MESSAGE";
    final static String ACCOUNTING_URL = "https://localhost:8443/accounting/rest/accounting/invoice/issue";


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
                .post().type(ObjednavkaDTO.class).outType(OutputResponse.class).to("direct:objednavka-process");


        //
        //SOAP endpoint
        //TODO udelat pres Cxf
//        rest("/ordersSOAP").consumes("application/soap").produces("application/soap")
//                .post().type(ObjednavkaDTO.class).outType(OutputResponse.class).to("direct:obj-preprocessSOAP");
//
//        CxfComponent cxfComponent = new CxfComponent(getContext());
//        CxfEndpoint serviceEndpoint = new CxfEndpoint("localhost", cxfComponent);
//        serviceEndpoint.setServiceClass(SOAPService.class);
//
//        errorHandler(noErrorHandler());
//        from("/ordersSOAP").to("direct:objednavka-process");




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
                .end()
                .setProperty("objednavka", body())
                .setHeader("objednavkaIn", body()) // zaloha vstupu
                .bean(ObjednavkaService.class, "isValid") //je vstup validni?
                .bean(ObjednavkaService.class, "create") //prevod na BO
                .to("direct:new-objednavka");


        //Spatna data
        from("direct:bad-request")
                .setHeader("description", simple("${body}"))
                .setHeader("status", simple("BAD_REQUEST"))
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400)) // BAD REQUEST
                .to("direct:createResponse");


        //Zpracovani polozek
        //
        from("direct:new-objednavka")
                //.transacted()
                .setHeader("objId", simple("${body.idObjednavka}"))
                .split(simple("${body.wantedItems}")/*.resultType(ItemBO.class)/**/, new AggregationStrategy() {
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
                }).streaming().convertBodyTo(ItemBO.class)
                    .log("[][][]>>Trying to process>>" + String.valueOf(simple("${body}")))
                    .inOut("direct:item-process")
                .end()
                .log(">>SPLITTED >>" + String.valueOf(simple("${body}")))

                //update splitted items
                .setProperty("id", simple("${header.objId}"))
                .setProperty("items", simple("${body}"))
                .bean(ObjednavkaService.class, "updateItems")

                //Check for VIPOrder
                .process(new ObjednavkaVIPCheckProcessor())
                .log("\n VIPOrder: ${header.VIPOrder} \n VIPuser: ${header.VIPuser}")

                .choice()
                    .when(simple("${header.VIPOrder} == true and ${header.VIPuser} == true")).log("VIP objednavka obdrzena")
                        .to("direct:accounting-insertion").endChoice()
                    .when(simple("${header.VIPOrder} == false")).log("Standardni objednavka obdrzena")
                        .to("direct:accounting-insertion").endChoice()
                    .otherwise().log("NonVIP customer VIP order attempt").endChoice()
                .end()

                .setBody(constant(null))
                .setHeader("status", simple("OK"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201)) // CREATED
                .to("direct:createResponse");


        //Zpracovani Itemu
        //
        from("direct:item-process").log(">>Item Processing>>" + String.valueOf(simple("${body}")))
                .setProperty("item", simple("${body}"))
                .bean(LocalStockService.class, "isInStock")
                .setBody(simple("false"))
                .choice()
                    .when(simple("${body} == true")).setBody(simple("${property.item}")).removeProperty("item").log(">>Item available locally")
                    .otherwise().setBody(simple("${property.item}")).removeProperty("item").log(">>Need to look to suppliers")
                .to("direct:item-suppliers-availability")
                .end();


        from("direct:item-suppliers-availability").log("checking Suppliers availability")
                //.transacted()
                .setHeader("localItem", simple("${body}"))

                //dotaz na Supplier-A
                .log("POLLING SupplierA")
//                .inOut(SUPPLIER_A_URL) //TODO volani supplierA
//                .unmarshal().soapjaxb()
                .setHeader("supplierAItem", simple("${body}"))
//                .setBody(constant(null))
                .log(String.valueOf(simple("${body}")))

                //dotaz na Supplier-B
                .log("POLLING SupplierB")
//                .inOut(SUPPLIER_B_URL) //TODO volani supplierB
//                .unmarshal().soapjaxb()
                .setHeader("supplierBItem", simple("${body}"))
                .log(String.valueOf(simple("${body}")))


                .setBody(constant(null))
                .log("L: ${header.localItem} sA: ${header.supplierAItem} sB: ${header.supplierBItem}")
                .process(new CheapestItemPickerProcessor());


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
        from("direct:expedition").log("Expedition attempt");
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
        from("direct:createResponse").log("Generating response")
                .setProperty("id", simple("${header.objId}"))
                .bean(ObjednavkaService.class, "get")
                .setHeader("objednavka", simple("${body}"))

                .setBody(constant(null))

                //Generovani odpovedi
                .bean(ResponseBuilder.class, "generateNewResponse")
                .choice()
                    .when(simple("${header.inputFormat} == 'SOAP' ")).marshal().soapjaxb().endChoice()
                    .otherwise()/*.marshal().json(JsonLibrary.Jackson, true)/**/.endChoice()
                .end()
                .log("Response generated")
                .removeHeader("*");
    }

}
