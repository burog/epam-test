/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this tweetsFile except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fadeevm.data.jpa;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = "fadeevm.data.jpa")
@EnableAutoConfiguration
@EnableAsync
@Slf4j
public class SampleDataJpaApplication {

  @Value("${maxPoolSize}")
  private int maxPoolSize;
  @Value("${queueCapacity}")
  private int queueCapacity;
  @Value("${corePoolSize}")
  private int corePoolSize;

  public static void main(String[] args) throws Exception {
    log.error("start context");

    long start = System.currentTimeMillis();
    SpringApplication.run(SampleDataJpaApplication.class, args);

    long totalTime = System.currentTimeMillis() - start;
    log.error("total execution time {} ms", totalTime);
  }

  @Bean(name = "threadPoolCsvTaskExecutor")
  public Executor threadPoolCsvTaskExecutor() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
    threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
    threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
    threadPoolTaskExecutor.setThreadNamePrefix("epam-");
    threadPoolTaskExecutor.initialize();
    return threadPoolTaskExecutor;
  }

}
