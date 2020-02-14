package csp.main;

import abscon.instance.tools.InstanceParser;
import utils.CliArgs;
import utils.LOG;

public class CSPSolver {
    public static String USAGE = "==";
    public static void main(String[] args) {

        CliArgs cliArgs = new CliArgs(args);

        String file = cliArgs.switchValue("-f", null);
        String acType = cliArgs.switchValue("-a", null);
        String printReport = cliArgs.switchValue("-print-report", null);
        boolean showInfo = cliArgs.switchPresent("-info");

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

        }
    }
}
