package Airtel;

import java.util.List;

import org.apache.camel.scr.AbstractCamelRunner;
import org.apache.camel.scr.ScrHelper;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class saiTest {

    Logger log = LoggerFactory.getLogger(getClass());

    @Rule
    public TestName testName = new TestName();

    sai integration;
    ModelCamelContext context;

    @Before
    public void setUp() throws Exception {
        log.info("*******************************************************************");
        log.info("Test: {}", testName.getMethodName());
        log.info("*******************************************************************");

        // Set property prefix for unit testing
        System.setProperty(sai.PROPERTY_PREFIX, "unit");

        // Prepare the integration
        integration = new sai();
        integration.prepare(null, ScrHelper.getScrProperties(integration.getClass().getName()));
        context = (ModelCamelContext) integration.getContext();

        // Configure this
        AbstractCamelRunner.configure(context, this, log);

        // Disable JMX for test
        context.disableJMX();
    }

    @After
    public void tearDown() throws Exception {
        integration.stop();
    }

	@Test
	public void testRoutes() throws Exception {
        // Adjust routes
        List<RouteDefinition> routes = context.getRouteDefinitions();

        routes.get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Replace "from" endpoint with direct:start
                replaceFromWith("direct:start");
                // Mock and skip result endpoint
                mockEndpoints("log:*");
            }
        });

        MockEndpoint resultEndpoint = context.getEndpoint("mock:log:foo", MockEndpoint.class);
        resultEndpoint.expectedBodiesReceived("hello");

        // Start the integration
        integration.run();

        // Send the test message
        context.createProducerTemplate().sendBody("direct:start", "hello");

        resultEndpoint.assertIsSatisfied();
	}
}
