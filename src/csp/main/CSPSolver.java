package csp.main;

import abscon.instance.components.PVariable;
import abscon.instance.tools.InstanceParser;
import utils.CliArgs;
import utils.LOG;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSPSolver {
    public static String USAGE =
            "./runProgram.sh -f {file_name}" +
                    " [-a {ac1 | ac3}] " +
                    " [-s {BT|CBJ|FC}]" +
                    " [-u {LX|LD|DEG|DD|dLX|dLD|dDEG|dDD}";
    private static LOG log;

    public static void main(String[] args) {
        log = new LOG(false);
        CliArgs cliArgs = new CliArgs(args);

        String file = cliArgs.switchValue("-f", null);
        String acType = cliArgs.switchValue("-a", "");
        String backtrack = cliArgs.switchValue("-s", "");
        String orderingHeuristic = cliArgs.switchValue("-u", null);
        String reportType = cliArgs.switchValue("-r", "debug");

        InstanceParser parser = new InstanceParser();
        String[] tokens = file.split("/");
        String fileName = tokens[tokens.length-1];
        if (file != null) {
            parser.loadInstance(file);
            parser.parse(false);
        } else {
            LOG.error(USAGE);
            return;
        }

        String presentationName = parser.getPresentationName();
        List<PVariable> variables = parser.getVariablesAsList();
        if (acType.equals("ac1")) {
            ArcConsistency AC = new ArcConsistency(
                    presentationName,
                    variables,
                    parser.getConstraintsAsList()
            );

            AC.arcConsistencyOne();
            LOG.stdout(AC.getReport());
            LOG.stdout(AC.getVarsDomain());
        } else if(acType.equals("ac3")) {
            ArcConsistency AC = new ArcConsistency(
                    presentationName,
                    variables,
                    parser.getConstraintsAsList()
            );
            //TODO: implement AC3.
        }

        List<String> staticOrderings = Arrays.asList(new String[]{"LX", "LD", "DEG", "DD"});
        if (orderingHeuristic != null && staticOrderings.contains(orderingHeuristic)) {
            log.debug("static order: " + orderingHeuristic);
            OrderingHeuristic oh = new OrderingHeuristic(variables);
            oh.run(orderingHeuristic);
            variables = oh.variables;
//            oh.printOrdering();
        }
        log.debug("PRint ordering....");
        printOrdering(variables);

        if (backtrack.equals("BT")) {
            BackTracking BT = new BackTracking(
                    presentationName,
                    variables,
                    parser.getConstraintsAsList()
            );
            BT.keepNodeConsistent();
            BT.run();
        } else if (backtrack.equals("CBJ")) {
            ConflictedBackJumping CBJ = new ConflictedBackJumping(
                    presentationName,
                    variables,
                    parser.getConstraintsAsList()
            );
            CBJ.keepNodeConsistent();
            CBJ.run();
        } else if (backtrack.equals("FC")) {
            List<String> dynamicOrdering = Arrays.asList(new String[]{"dLX", "dLD", "dDEG", "dDD"});
            if (dynamicOrdering.contains(orderingHeuristic)) {
                DynamicFC fc = new DynamicFC(
                        orderingHeuristic,
                        fileName,
                        variables,
                        parser.getConstraintsAsList()
                );
                fc.orderingHeuristic = orderingHeuristic;
                fc.keepNodeConsistent();
                String report = fc.run(reportType);
                LOG.stdout(report);
            } else {
                FC fc = new FC(fileName, variables, parser.getConstraintsAsList());
                fc.orderingHeuristic = orderingHeuristic;
                fc.keepNodeConsistent();
                String report = fc.run(reportType);
                LOG.stdout(report);
            }
        } else {
            LOG.error("Usage: " + USAGE);
        }
    }
    public static void printOrdering(List<PVariable> vars) {
        log.debug(varNameList(vars));
    }
    private static String varNameList(List<PVariable> vars) {
        return vars.stream()
                .map(var -> var.getName())
                .collect(Collectors.joining(", "));
    }
}
