package ru.clevertec.data.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.entity.Customer;

@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final DataSource dataSource;

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_BIRTH_DATE = "birth_date";

    private static final String CREATE = """
            INSERT INTO customers (first_name, last_name, birth_date, email)
            VALUES (?, ?, ?, ?)
            """;

    private static final String FIND_ALL = """
            SELECT id, first_name, last_name, birth_date, email
            FROM customers
            WHERE deleted = false
            ORDER by u.id
            LIMIT ?
            OFFSET ?
            """;

    private static final String FIND_BY_ID = """
            SELECT id, first_name, last_name, birth_date, email
            FROM customers
            WHERE id = ? AND deleted = false
            """;

    private static final String UPDATE = """
            UPDATE customers
            SET first_name = ?, last_name = ?, birth_date = ?, email = ?
            WHERE id = ?
            """;

    private static final String DELETE_BY_ID = """
            UPDATE customers
            SET deleted = true
            WHERE id = ?
            """;


    @Override
    public Customer create(Customer customer) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setDate(3, Date.valueOf(customer.getDateBirth()));
            statement.setString(4, customer.getEmail());
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            keys.next();
            Long id = keys.getLong(COLUMN_ID);
            customer.setId(id);
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Customer> findAll(int limit, long offset) {
        List<Customer> list = new ArrayList<>();
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            statement.setInt(1, limit);
            statement.setLong(2, offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(process(resultSet));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(process(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Customer update(Customer customer) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setDate(3, Date.valueOf(customer.getDateBirth()));
            statement.setString(4, customer.getEmail());
            statement.setLong(5, customer.getId());
            statement.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Customer process(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setId(resultSet.getLong(COLUMN_ID));
        customer.setFirstName(resultSet.getString(COLUMN_FIRST_NAME));
        customer.setLastName(resultSet.getString(COLUMN_LAST_NAME));
        customer.setEmail(resultSet.getString(COLUMN_EMAIL));
        customer.setDateBirth(resultSet.getDate(COLUMN_BIRTH_DATE).toLocalDate());
        return customer;
    }
}
