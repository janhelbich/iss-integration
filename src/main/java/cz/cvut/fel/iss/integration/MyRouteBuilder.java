package cz.cvut.fel.iss.integration;

import cz.cvut.fel.iss.integration.model.Objednavka;
import cz.cvut.fel.iss.integration.model.RESTResponse;
import cz.cvut.fel.iss.integration.model.exceptions.InvalidObjednavkaDataFormat;
import cz.cvut.fel.iss.integration.service.ObjednavkaService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import javax.ejb.Singleton;
import javax.ejb.Startup;

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
                .bindingMode(RestBindingMode.json)
                .port(8080)

                .dataFormatProperty("prettyPrint", "true")
                .dataFormatProperty("include", "NON_NULL")
                .dataFormatProperty("json.in.disableFeatures", "FAIL_ON_UNKNOWN_PROPERTIES");


        //
        //JSON endpoint
        //
        rest("/ordersJSON").consumes("application/json").produces("application/json")
                .post().type(Objednavka.class).outType(RESTResponse.class).to("direct:objednavka-process");


        //
        //SOAP endpoint
        //
        rest("/ordersSOAP").consumes("application/soap").produces("application/soap")
                .post().type(Objednavka.class).outType(RESTResponse.class).to("direct:objednavka-process");


        //
        //ROUTY
        //TODO - tady dodelat logiku, workflow - podle toho se zde bude volat na jednotlive sluzby
        // (a nebo to poslat do dalsich rout)


        //Zpracovani objednavky
        from("direct:objednavka-process")
                .setProperty("objednavka", simple("${body}"))
//                .bean(ObjednavkaService.class, "isValid") //je validni? TODO dodelat metodu isValid(Objednavka objednavka)
                .onException(InvalidObjednavkaDataFormat.class).handled(true)
                    .to("direct:bad-request")
                .end()
                .to("direct:new-objednavka")
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201)); // OK

        //Spatna data
        from("direct:bad-request")
                .setBody(constant(null))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400)); // BAD REQUEST

        //Zpracovani polozek
        //TODO
//        from("direct:new-objednavka")
//                .bean(ObjednavkaService.class, "processObjednavka")
//                .transacted()
//                .split()...
//                ...
//                .inOut("direct:item-process")
//                .onException(Exception.class)
//                .end();

        //Zpracovani Itemu
        //TODO
//        from("direct:item-process")
//                .transacted()
//                .inOut("A MQ NEBO ADAPTER SUPPLIER A")
//                .setProperty("supplierAPrice", simple("${body}")).setBody(constant(null))
//                .inOut("A MQ NEBO ADAPTER SUPPLIER B")
//                .setProperty("supplierBPrice", simple("${body}")).setBody(constant(null))
//                .choice()
//                    .when(header("customerStatus").isEqualTo(simple("VIP"))).bean(ObjednavkaService.class, "compareAndReturnLowestPrice")
//                .end()
//                .onException(Exception.class)
//                .end();







        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        from("file:src/data?noop=true")
            .choice()
                .when(xpath("/person/city = 'London'"))
                    .to("file:target/messages/uk")
                .otherwise()
                    .to("file:target/messages/others");
    }

}
