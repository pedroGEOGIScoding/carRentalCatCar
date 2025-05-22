package com.pedro.apps.delegations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DelegationRepositoryImpl implements DelegationRepository {

	private final DynamoDbEnhancedClient enhancedClient;
	private final String tableName = "Delegations";

	@Autowired
	public DelegationRepositoryImpl(DynamoDbEnhancedClient enhancedClient) {
		this.enhancedClient = enhancedClient;
	}

	@Override
	public <T> void save(T item) {
		DynamoDbTable<T> table =
				enhancedClient.table(
						tableName,
						TableSchema.fromBean((Class<T>) item.getClass()));
		table.putItem(item);
	}

	@Override
	public <T> T get(String partitionKey, String sortKey, Class<T> clazz) {
		DynamoDbTable<T> table = enhancedClient.table(
				tableName, TableSchema.fromBean(clazz));
		Key key = Key.builder()
				.partitionValue(partitionKey)
				.sortValue(sortKey)
				.build();
		return table.getItem(key);
	}

	@Override
	public <T> List<T> listByPartitionKey(String partitionKey, Class<T> clazz) {
		DynamoDbTable<T> table = enhancedClient.table(tableName, TableSchema.fromBean(clazz));
		QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue(partitionKey));
		List<T> items = new ArrayList<>();
		table.query(queryConditional).items().forEach(items::add);
		return items;
	}

	@Override
	public <T> List<T> listAll(Class<T> clazz) {
		return List.of();
	}

	@Override
	public List<Car> listAllCars() {
		// Create a DynamoDB table object for the Car class, mapping to the "Delegations" table
		DynamoDbTable<Car> table = enhancedClient.table(tableName, TableSchema.fromBean(Car.class));
		// Initialize an empty ArrayList to store the retrieved Car objects
		List<Car> cars = new ArrayList<>();
		// Create a HashMap to store expression values for the filter expression
		Map<String, AttributeValue> expressionValues = new HashMap<>();
		// Add a key-value pair to the map, where ":val" is the placeholder for the string "car"
		expressionValues.put(":val", AttributeValue.builder().s("car").build());
		// Build a filter expression to match items where the "operation" sort key begins with "car"
		Expression filterExpression = Expression.builder()
				.expression("begins_with(operation, :val)") // Define the expression using the begins_with function
				.expressionValues(expressionValues) // Associate the expression values map
				.build(); // Construct the Expression object
		// Build a ScanEnhancedRequest with the filter expression to limit results to Car items
		ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
				.filterExpression(filterExpression) // Apply the filter expression to the scan
				.build(); // Construct the ScanEnhancedRequest object
		// Execute the scan operation and iterate over the results, adding each Car item to the cars list
		table.scan(scanRequest).items().forEach(cars::add);
		// Return the list of Car objects
		return cars;
	}

	@Override
	public List<Delegation> listAllDelegations() {
		DynamoDbTable<Delegation> table = enhancedClient.table(tableName, TableSchema.fromBean(Delegation.class));
		List<Delegation> delegations = new ArrayList<>();
		Map<String, AttributeValue> expressionValues = new HashMap<>();
		expressionValues.put(":val", AttributeValue.builder().s("profile").build());
		Expression filterExpression = Expression.builder()
				.expression("operation = :val")
				.expressionValues(expressionValues)
				.build();
		ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
				.filterExpression(filterExpression)
				.build();
		table.scan(scanRequest).items().forEach(delegations::add);
		return delegations;
	}

	@Override
	public <T> List<T> listAllItems(Class<T> clazz) {
		DynamoDbTable<T> table = enhancedClient.table(tableName, TableSchema.fromBean(clazz));
		List<T> items = new ArrayList<>();
		table.scan(ScanEnhancedRequest.builder().build()).items().forEach(items::add);
		return items;
	}

}




