package ws.cogito.magic;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.model.dataformat.XmlJsonDataFormat;

/**
 * Magic Route Builder
 */
public class MagicRouteBuilder extends RouteBuilder {
	
	public static String splitXpath = "//orders/order";

    public void configure() {
    	
    	/**
    	 * Route errors to DLQ after one retry and one second delay
    	 */
    	errorHandler(deadLetterChannel("activemq:emagic.dead").
    			maximumRedeliveries(1).redeliveryDelay(1000));
    	
		XPathBuilder splitXPath = new XPathBuilder (splitXpath);
		
    	/**
    	 * Splitter - xpath expression
    	 */
		from("activemq:emagic.orders").
			split(splitXPath).parallelProcessing().
			wireTap("direct:ministry").
		to("activemq:emagic.order");
    	
    	/**
    	 * Content Based Routing - simple expression
    	 */
    	from("activemq:emagic.order").
    	choice().
    		when().simple("${in.body} contains 'Houdini'").
    			to("activemq:priority.order").
    		otherwise().
    			to("activemq:magic.order");
    	/**
    	 * Content Based Routing - Mediation, simple expression
	 
	XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
	xmlJsonFormat.setForceTopLevelObject(true);
		
    	from("activemq:emagic.order").
    	choice().
    		when().simple("${in.body} contains 'Houdini'").
				process(new Processor() {
	        		public void process(Exchange exchange) {	
	        			exchange.getIn().setHeader("VIP", "true");
	        		}
	        	}).
    			to("activemq:priority.order").
    		otherwise().
    			marshal(xmlJsonFormat).
    			transform(body().regexReplaceAll("@", "")).
    			to("activemq:magic.order");*/
    	
    	
    	/**
    	 * Content Based Routing - Wire-Tap to ActiveMQ Topic 
    	 
    	from("activemq:emagic.order").
    		wireTap("direct:ministry").
    	to("activemq:magic.order");
    	
    	from("direct:ministry").
			choice().
				when().simple("${in.body} contains 'Elder Wand'").
					log("ILLEGAL MAGIC ALERT").
					to("activemq:topic:magic.alerts").		
				otherwise().
					log("...off into the ether");*/	
    }
}