package de.rub.nds.ssl.analyzer.executor;

import de.rub.nds.ssl.analyzer.AnalyzerResult;
import de.rub.nds.ssl.analyzer.fingerprinter.ETLSImplementation;
import de.rub.nds.ssl.analyzer.fingerprinter.ScoreCounter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.apache.log4j.Logger;

/**
 * Reporter summarizes analyzer results and generates a report.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Jan 30, 2013
 */
public final class Reporter {

    /**
     * Utility class - private constructor only.
     */
    private Reporter() {
    }

    /**
     * Compresses multiple score counter of different AnalyzerResult arrays to a
     * single ScoreCounter.
     *
     * @param results Multiple AnalyzerResults containing ScoreCounters
     * @return Combined ScoreCounter
     */
    private static ScoreCounter compress(final AnalyzerResult[] results) {
        ScoreCounter result = new ScoreCounter();

        // sum up the results
        ScoreCounter tmpSC;
        for (AnalyzerResult tmpResult : results) {
            tmpSC = tmpResult.getScoreCounter();
            if (tmpSC != null) {
                for (ETLSImplementation impl : ETLSImplementation.values()) {
                    result.countResult(impl, tmpSC.getScore(impl));
                }
            }
        }

        return result;
    }

    /**
     * Generates a summarizing report of all AnalyzerResults.
     *
     * @param results Multiple AnalyzerResults containing ScoreCounters
     * @param logger Logger to be used for report output
     */
    public static void generateReport(final AnalyzerResult[] results,
            final Logger logger) {
        logger.info("########################################################"
                + "################");
        logger.info("Final analyzer Results");
        logger.info("########################################################"
                + "################");
        // sum up the results
        ScoreCounter scoreCounter = compress(results);
//        int totalScore = scoreCounter.getTotalCounter();
        int totalScore = results.length;
        
        // output results
        int tmpScore;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat twoDForm = new DecimalFormat("###.##", symbols);
        for (ETLSImplementation impl : ETLSImplementation.values()) {
            tmpScore = scoreCounter.getScore(impl);

            logger.info(String.format("%6s %% ( %2d / %2d ) match "
                    + "with: %s", twoDForm.format((double) tmpScore
                    / (double) totalScore * 100), tmpScore, totalScore,
                    impl.name()));
        }

        tmpScore = scoreCounter.getNoHitCounter();
        logger.info(String.format("%6s %% ( %2d / %2d ) is unknown "
                + "behaviour.", twoDForm.format((double) tmpScore
                / (double) totalScore * 100), tmpScore, totalScore));
    }
}
