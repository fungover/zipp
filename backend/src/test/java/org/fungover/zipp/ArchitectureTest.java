package org.fungover.zipp;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.java.ImportsConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.JUnitConfigurer;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {

	@Test
	void shouldFulfillArchitectureConstraints() {
		Taikai.builder().namespace("org.fungover.zipp")
				.java(java -> java.noUsageOfDeprecatedAPIs().imports(ImportsConfigurer::shouldHaveNoCycles))
				.spring(SpringConfigurer::noAutowiredFields)
				.test(test -> test.junit(JUnitConfigurer::methodsShouldNotBeAnnotatedWithDisabled)).build().check();
	}
}
