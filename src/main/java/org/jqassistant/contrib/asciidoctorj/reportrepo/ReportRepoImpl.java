package org.jqassistant.contrib.asciidoctorj.reportrepo;

import com.buschmais.jqassistant.core.rule.api.filter.RuleFilter;
import lombok.Getter;
import org.jqassistant.contrib.asciidoctorj.processors.attributes.ProcessAttributes;
import org.jqassistant.contrib.asciidoctorj.reportrepo.model.*;
import org.jqassistant.contrib.asciidoctorj.xmlparsing.ParsedReport;
import org.jqassistant.contrib.asciidoctorj.xmlparsing.ReportParser;

import java.util.*;

@Getter
public class ReportRepoImpl implements ReportRepo{

    private boolean initialized = false;

    private static final RuleFilter RULE_FILTER = RuleFilter.getInstance();

    private final ReportParser reportParser;

    private Map<String, Group> groups = new HashMap<>();
    private Map<String, Concept> concepts = new HashMap<>();
    private Map<String, Constraint> constraints = new HashMap<>();

    public ReportRepoImpl(ReportParser reportParser) {
        this.reportParser = reportParser;
    }

    private void initialize(ProcessAttributes attributes) {
        if (!isInitialized()) {
            ParsedReport report = reportParser.parseReportXml(attributes.getReportPath());
            this.groups = report.getGroups();
            this.concepts = report.getConcepts();
            this.constraints = report.getConstraints();

            initialized = true;
        }
    }

    @Override
    public SortedSet<Concept> findConcepts(ProcessAttributes attributes) {
        initialize(attributes);

        SortedSet<Concept> conceptSSet = new TreeSet<>(Comparator.comparing(Rule::getId));

        if(attributes.getConceptIdFilter() == null) {
            conceptSSet.addAll(concepts.values());
            return conceptSSet;
        }

        String id = attributes.getConceptIdFilter();

        conceptSSet.addAll((Collection<Concept>) filterRulesById(concepts, id));

        return conceptSSet;
    }

    @Override
    public SortedSet<Constraint> findConstraints(ProcessAttributes attributes) {
        initialize(attributes);

        SortedSet<Constraint> constraintSSet = new TreeSet<>(Comparator.comparing(Rule::getId));

        if(attributes.getConstraintIdFilter() == null) {
            constraintSSet.addAll(constraints.values());
            return constraintSSet;
        }

        String id = attributes.getConstraintIdFilter();

        constraintSSet.addAll((Collection<Constraint>) filterRulesById(constraints, id));

        return constraintSSet;
    }

    private Collection<? extends Rule> filterRulesById(Map<String, ? extends Rule> ruleMap, String id) {
        if(id == null) return ruleMap.values();

        Set<String> matchingIds = RULE_FILTER.match(ruleMap.keySet(), id);

        List<Rule> matchingRules = new ArrayList<>();
        matchingIds.forEach(s -> matchingRules.add(ruleMap.get(s)));

        return matchingRules;
    }
}
