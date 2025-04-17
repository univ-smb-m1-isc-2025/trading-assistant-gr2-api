package com.traderalerting.repository;

import com.traderalerting.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    Optional<Symbol> findByTicker(String ticker);
    boolean existsByTicker(String ticker);
}