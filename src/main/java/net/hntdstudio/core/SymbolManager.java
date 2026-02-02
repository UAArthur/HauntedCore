package net.hntdstudio.core;

import net.hntdstudio.core.symbols.BaseSymbol;
import net.hntdstudio.core.symbols.ExclamationSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SymbolManager {
    private final Main main;
    //SYMBOLS
    private final Map<String, BaseSymbol> symbols;
    private int clientEntityIdCounter = 1000000;


    public SymbolManager(Main main) {
        this.main = main;
        this.symbols = new HashMap<>();

        registerSymbol("exclamation", new ExclamationSymbol("ExclamationMark", 2.5f, 1.5f));
    }

    private void registerSymbol(String id, BaseSymbol symbol) {
        symbols.put(id.toLowerCase(), symbol);
    }

    public Optional<BaseSymbol> getSymbol(String id) {
        return Optional.ofNullable(symbols.get(id.toLowerCase()));
    }

    public boolean hasSymbol(String id) {
        return symbols.containsKey(id.toLowerCase());
    }

    public Map<String, BaseSymbol> getAllSymbols() {
        return new HashMap<>(symbols);
    }

    public int getNextNetworkId() {
        return clientEntityIdCounter++;
    }
}
