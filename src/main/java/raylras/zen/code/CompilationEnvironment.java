package raylras.zen.code;

import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raylras.zen.code.bracket.BracketHandlerManager;
import raylras.zen.code.symbol.ClassSymbol;
import raylras.zen.code.symbol.Symbol;
import raylras.zen.code.type.ClassType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CompilationEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(CompilationEnvironment.class);

    public static final String DEFAULT_ROOT_DIRECTORY = "scripts";
    public static final String DEFAULT_GENERATED_DIRECTORY = "generated";

    private final Path root;
    private final Map<Path, CompilationUnit> unitMap = new HashMap<>();
    private BracketHandlerManager bracketHandlerManager;

    public CompilationEnvironment(Path root) {
        Objects.requireNonNull(root);
        this.root = root;
    }

    public CompilationUnit createUnit(Path unitPath) {
        CompilationUnit unit = new CompilationUnit(unitPath, this);
        unitMap.put(unitPath, unit);
        return unit;
    }

    public CompilationUnit getUnit(Path unitPath) {
        return unitMap.get(unitPath);
    }

    public void removeUnit(Path unitPath) {
        unitMap.remove(unitPath);
    }

    public Collection<CompilationUnit> getUnits() {
        return unitMap.values();
    }

    public Map<Path, CompilationUnit> getUnitMap() {
        return unitMap;
    }

    public List<Symbol> getGlobalSymbols() {
        return getUnits().stream()
                .flatMap(unit -> unit.getTopLevelSymbols().stream())
                .filter(symbol -> symbol.isModifiedBy(Symbol.Modifier.GLOBAL))
                .collect(Collectors.toList());
    }

    public Map<String, ClassType> getClassTypeMap() {
        return getUnits().stream()
                .flatMap(unit -> unit.getTopLevelSymbols().stream())
                .filter(symbol -> symbol instanceof ClassSymbol)
                .map(symbol -> (ClassSymbol) symbol)
                .collect(Collectors.toMap(ClassSymbol::getQualifiedName, ClassSymbol::getType));
    }

    public Path getRoot() {
        return root;
    }

    public Path getGeneratedRoot() {
        return root.resolve(DEFAULT_GENERATED_DIRECTORY);
    }

    public BracketHandlerManager getBracketHandlerManager() {
        if (bracketHandlerManager == null) {
            bracketHandlerManager = loadBracketHandlerManager();
        }
        return bracketHandlerManager;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    private BracketHandlerManager loadBracketHandlerManager() {
        try {
            BufferedReader jsonReader = Files.newBufferedReader(getGeneratedRoot().resolve("brackets.json"));
            return BracketHandlerManager.GSON.fromJson(jsonReader, BracketHandlerManager.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.error("Failed to open brackets.json: {}", this, e);
        }
        return new BracketHandlerManager(Collections.emptyList());
    }

}
