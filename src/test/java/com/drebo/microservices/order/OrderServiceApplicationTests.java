package com.drebo.microservices.order;

import com.drebo.microservices.order.service.OrderService;
import com.drebo.microservices.order.stub.InventoryStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;

import static org.hamcrest.MatcherAssert.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@Autowired
	MySQLContainer<?> mySQLContainer;

	@Autowired
	OrderService orderService;

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setup(){
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@AfterEach
	void cleanup(){
		orderService.deleteAllOrders();
	}

	@Test
	void isRunning() {
		assert mySQLContainer.isRunning();
	}

	@Test
	void placeOrderTest(){
		String orderDto = """
				{
				    "sku": "test1",
				    "price": "69.99",
				    "quantity": "10"
				}
				""";

		//mock response from inventory service
		InventoryStub.stubInventoryCall("test1", 10);

		var response = RestAssured.given()
				.contentType("application/json")
				.body(orderDto)
				.when()
				.post("api/order")
				.then()
				.log().all()
				.statusCode(201)
				.extract()
				.body().asString();

		assertThat(response, Matchers.is("Order placed successfully"));
	}

}
