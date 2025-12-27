package com.pluta.camera.services;

import com.pluta.camera.context.TenantContext;
import com.pluta.camera.entities.Widget;
import com.pluta.camera.repositories.WidgetRepository;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class QueryExecutionService {

    private final WidgetRepository widgetRepository;

    @PersistenceContext
    private final EntityManager entityManager;


    @Transactional(readOnly = true)
    public List<Map<String, Object>> executeQuery(String queryName, Map<String, Object> parameters) {
        Widget dynamicQuery = widgetRepository.findByQueryName(queryName)
                .orElseThrow(() -> new IllegalArgumentException("Query not found with queryName: " + queryName));


        Session session = entityManager.unwrap(Session.class);

        // Create query using Tuple to get column names
        Query<Tuple> query = session.createQuery(dynamicQuery.getHqlQuery(), Tuple.class);

        query.setParameter("tenantId", TenantContext.getTenantId());
        query.setParameter("branchId", TenantContext.getBranchId());

        // Set parameters if provided
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(query::setParameter);
        }

        List<Tuple> tuples = query.getResultList();

        // Convert tuples to List of Maps
        List<Map<String, Object>> results = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> row = new LinkedHashMap<>();

            // Get all elements with their aliases/column names
            for (TupleElement<?> element : tuple.getElements()) {
                String columnName = element.getAlias();
                Object value = tuple.get(element);
                row.put(columnName, value);
            }

            results.add(row);
        }

        return results;
    }

}
