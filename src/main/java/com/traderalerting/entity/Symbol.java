package com.traderalerting.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "symbols")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Symbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    @EqualsAndHashCode.Include
    private String ticker;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    public Symbol(String ticker, String name) {
        this.ticker = ticker;
        this.name = name;
    }
}