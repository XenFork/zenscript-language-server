package raylras.zen.util;

import raylras.zen.code.symbol.FunctionSymbol;
import raylras.zen.code.symbol.ParameterSymbol;
import raylras.zen.code.symbol.Symbol;
import raylras.zen.code.type.SubtypeResult;
import raylras.zen.code.type.Type;

import java.util.List;
import java.util.stream.Collectors;

public class Symbols {

    public static <T extends Symbol> List<T> getMembersByName(Type type, String simpleName, Class<T> clazz) {
        return type.getMembers().stream()
                .filter(symbol -> symbol.getSimpleName().equals(simpleName))
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public static boolean areArgumentsMatch(FunctionSymbol function, List<Type> argumentTypeList) {
        List<ParameterSymbol> parameterList = function.getParameterList();
        for (int i = 0; i < parameterList.size(); i++) {
            ParameterSymbol parameter = parameterList.get(i);
            if (i < argumentTypeList.size()) {
                Type argument = argumentTypeList.get(i);
                if (!argument.isAssignableTo(parameter.getType())) {
                    return false;
                }
            } else if (!parameter.isOptional()) {
                return false;
            }
        }
        return true;
    }

    public static FunctionSymbol findBestMatch(List<FunctionSymbol> functions, List<Type> argumentTypeList) {
        FunctionSymbol found = null;
        SubtypeResult foundMatchingResult = SubtypeResult.MISMATCH;
        for (FunctionSymbol function : functions) {
            List<ParameterSymbol> parameterList = function.getParameterList();
            SubtypeResult functionMatchingResult = SubtypeResult.SELF;
            if (argumentTypeList.size() > parameterList.size()) {
                continue;
            }
            for (int i = 0; i < parameterList.size(); i++) {
                if (i < argumentTypeList.size()) {
                    functionMatchingResult = functionMatchingResult.and(argumentTypeList.get(i).isSubtypeOf(parameterList.get(i).getType()));
                } else {
                    functionMatchingResult = functionMatchingResult.and(parameterList.get(i).isOptional() ? SubtypeResult.SELF : SubtypeResult.MISMATCH);
                }
            }
            if (functionMatchingResult.priority < foundMatchingResult.priority) {
                found = function;
                foundMatchingResult = functionMatchingResult;
            }
        }
        return found;
    }

    public static Type predictNextArgumentType(List<FunctionSymbol> functions, List<Type> argumentTypes) {
        Type found = null;
        SubtypeResult foundMatchingResult = SubtypeResult.MISMATCH;
        for (FunctionSymbol function : functions) {
            List<ParameterSymbol> parameterList = function.getParameterList();
            SubtypeResult functionMatchingResult = SubtypeResult.SELF;
            if (argumentTypes.size() >= parameterList.size()) {
                continue;
            }
            for (int i = 0; i < argumentTypes.size(); i++) {
                Type argType = argumentTypes.get(i);
                Type paramType = parameterList.get(i).getType();
                functionMatchingResult = functionMatchingResult.and(argType.isSubtypeOf(paramType));
            }
            if (functionMatchingResult.priority < foundMatchingResult.priority) {
                found = parameterList.get(argumentTypes.size()).getType();
                foundMatchingResult = functionMatchingResult;
            }
        }
        return found;
    }
}
