package csp.main;

import abscon.instance.tools.InstanceParser;
import utils.CliArgs;
import utils.LOG;

public class CSPSolver {
    public static String USAGE =
            "./runProgram.sh -f {file_name}" +
                    " [-a {ac1 | ac3}] " +
                    " [-s {BT}]";
    public static void main(String[] args) {

        CliArgs cliArgs = new CliArgs(args);

        String file = cliArgs.switchValue("-f", null);
        String acType = cliArgs.switchValue("-a", "");
        String backtrack = cliArgs.switchValue("-s", "");
        String orderingHeuristic = cliArgs.switchValue("-u", "");

        InstanceParser parser = new InstanceParser();
        if (file != null) {
            parser.loadInstance(file);
            parser.parse(false);
        } else {
            LOG.error(USAGE);
            return;
        }

        if (acType.equals("ac1")) {
            ArcConsistency AC = new ArcConsistency(
                    parser.getPresentationName(),
                    parser.getVariablesAsList(),
                    parser.getConstraintsAsList()
            );

            AC.arcConsistencyOne();
            LOG.stdout(AC.getReport());
            LOG.stdout(AC.getVarsDomain());
        } else if(acType.equals("ac3")) {
            ArcConsistency AC = new ArcConsistency(
                    parser.getPresentationName(),
                    parser.getVariablesAsList(),
                    parser.getConstraintsAsList()
            );
            //TODO: implement AC3.
        }

        if (backtrack.equals("BT")) {
            BackTracking BT = new BackTracking(
                    parser.getPresentationName(),
                    parser.getVariablesAsList(),
                    parser.getConstraintsAsList()
            );
            BT.keepNodeConsistent();

            if (!orderingHeuristic.equals("")) {
                BT.preOrderVariableOrValue(orderingHeuristic);
            }

            BT.bcssp();
        }


    }
}
