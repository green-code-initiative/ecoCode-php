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
package fr.greencodeinitiative.php;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class PhpRuleRepositoryTest {

    private RulesDefinition.Repository repository;

    @BeforeEach
    void init() {
        final SonarRuntime sonarRuntime = mock(SonarRuntime.class);
        doReturn(Version.create(0, 0)).when(sonarRuntime).getApiVersion();
        PhpRuleRepository rulesDefinition = new PhpRuleRepository(sonarRuntime);
        RulesDefinition.Context context = new RulesDefinition.Context();
        rulesDefinition.define(context);
        repository = context.repository(rulesDefinition.repositoryKey());
    }

    @Test
    @DisplayName("Test repository metadata")
    void testMetadata() {
        assertThat(repository.name()).isEqualTo("ecoCode");
        assertThat(repository.language()).isEqualTo("php");
        assertThat(repository.key()).isEqualTo("ecocode-php");
    }

    @Test
    void testRegistredRules() {
        assertThat(repository.rules()).hasSize(10);
    }

    @Test
    @DisplayName("All rule keys must be prefixed by 'EC'")
    void testRuleKeyPrefix() {
        SoftAssertions assertions = new SoftAssertions();
        repository.rules().forEach(
                rule -> assertions.assertThat(rule.key()).startsWith("EC")
        );
        assertions.assertAll();
    }

    @Test
    void testAllRuleParametersHaveDescription() {
        SoftAssertions assertions = new SoftAssertions();
        repository.rules().stream()
                .flatMap(rule -> rule.params().stream())
                .forEach(param -> assertions.assertThat(param.description())
                        .as("description for " + param.key())
                        .isNotEmpty());
        assertions.assertAll();
    }
}
