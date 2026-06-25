package br.com.valora.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita execução assíncrona para o AuditService.
 * Garante que os logs não impactem a performance das operações.
 */
@Configuration
@EnableAsync
public class AsyncConfig {}
