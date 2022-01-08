

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.stream.Stream;
import java.io.IOException;


public class CustomVerificationTest {
  
    @ParameterizedTest
    @MethodSource("verificationTestsProvider")
    public void runTest(String testId, String input, String expectedOutput) throws IOException {

        String stdOut = getOrderBookOutput(input);
        System.out.println(stdOut);
        assertEquals(expectedOutput, stdOut);
    }

    private static String getOrderBookOutput(String input) throws IOException {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;

        try (
            ByteArrayInputStream newIn = new ByteArrayInputStream(input.getBytes());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true)
        ){
            System.setIn(newIn);
            System.setOut(ps);

            SETSOrderBookExercise.main(new String[0]);

            return baos.toString(StandardCharsets.UTF_8.name()).trim().replaceAll("\\r\\n?", "\n");

        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    static Stream<Arguments> verificationTestsProvider() {
        return Stream.of(
            Arguments.of("IcebergOrder", 
                ""
                .concat("S,3,2,12\n")
                .concat("B,1,2,100000,11\n")
                .concat("B,2,2,10000\n")
                .concat("S,6,2,12\n")
                .concat("S,4,2,88\n")
                .concat("S,5,2,100000,12\n"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |      2|           12|         3|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,3,2,12\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           11|      2|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           11|      2|       |             |          |\n")
                .concat("|         2|       10,000|      2|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,6,2,12\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           11|      2|       |             |          |\n")
                .concat("|         2|       10,000|      2|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,4,2,88\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           11|      2|       |             |          |\n")
                .concat("|         2|       10,000|      2|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,5,2,99888\n")
                .concat("2,5,2,112\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         2|        9,888|      2|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("PriorityOrder", 
                ""
                .concat("S,1,14,12\n")
                .concat("S,2,12,12\n")
                .concat("S,3,13,12\n")
                .concat("B,4,9,100000,11\n")
                .concat("B,5,8,10000\n")
                .concat("B,6,10,12\n")
                .concat("B,7,11,88\n")
                .concat("B,8,13,100000,12\n"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |     14|           12|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |     12|           12|         2|\n")
                .concat("|          |             |       |     14|           12|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |     12|           12|         2|\n")
                .concat("|          |             |       |     13|           12|         3|\n")
                .concat("|          |             |       |     14|           12|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         4|           11|      9|     12|           12|         2|\n")
                .concat("|          |             |       |     13|           12|         3|\n")
                .concat("|          |             |       |     14|           12|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         4|           11|      9|     12|           12|         2|\n")
                .concat("|         5|       10,000|      8|     13|           12|         3|\n")
                .concat("|          |             |       |     14|           12|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         6|           12|     10|     12|           12|         2|\n")
                .concat("|         4|           11|      9|     13|           12|         3|\n")
                .concat("|         5|       10,000|      8|     14|           12|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         7|           88|     11|     12|           12|         2|\n")
                .concat("|         6|           12|     10|     13|           12|         3|\n")
                .concat("|         4|           11|      9|     14|           12|         1|\n")
                .concat("|         5|       10,000|      8|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("8,2,13,12\n")
                .concat("8,3,13,12\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         8|           12|     13|     14|           12|         1|\n")
                .concat("|         7|           88|     11|       |             |          |\n")
                .concat("|         6|           12|     10|       |             |          |\n")
                .concat("|         4|           11|      9|       |             |          |\n")
                .concat("|         5|       10,000|      8|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            )
        );
    }
}