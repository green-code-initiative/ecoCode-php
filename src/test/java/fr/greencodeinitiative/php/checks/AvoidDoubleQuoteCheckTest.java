/*
 * ecoCode - PHP language - Provides rules to reduce the environmental footprint of your PHP programs
 * Copyright © 2023 Green Code Initiative (https://www.ecocode.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.greencodeinitiative.php.checks;

import java.io.File;

import org.junit.Test;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.tests.PhpTestFile;

public class AvoidDoubleQuoteCheckTest {

    @Test
    public void test() throws Exception {
        PHPCheckTest.check(new AvoidDoubleQuoteCheck(), new PhpTestFile(new File("src/test/resources/checks/AvoidDoubleQuote.php")));
    }

}
