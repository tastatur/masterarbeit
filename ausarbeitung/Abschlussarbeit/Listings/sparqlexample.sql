PREFIX dbo: <http://dbpedia.org/ontology/>

SELECT ?areaTotal ?bundesland WHERE {
 ?entity rdfs:label "Duisburg"@de.
 ?entity dbo:areaTotal ?areaTotal.
 ?entity dbo:federalState ?bundesland.
}
