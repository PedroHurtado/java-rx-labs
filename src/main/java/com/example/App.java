package com.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        /*
         * function sum(a){
         * return function(b){
         * return a+b
         * }
         * }
         * 
         */
        /*
         * Function<Integer, Function<Integer, Integer>> sum = a -> b -> a + b;
         * 
         * var result = sum.apply(5);
         * System.out.println(result.apply(100));
         * System.out.println(result.apply(30));
         * System.out.println(result.apply(40));
         */

        System.out.println("\n");
        List<Integer> a = Arrays.asList(1, 2, 3, 4);
        List<Integer> b = Arrays.asList(1, 2);

        // INNER JOIN
        List<Integer> innerJoin = a.stream()
                .filter(b::contains)
                .collect(Collectors.toList());

        System.out.println("INNER JOIN: " + innerJoin); // [1, 2]

        // LEFT OUTER JOIN (con indicador de match)
        List<Map<String, Object>> leftOuterJoin = a.stream()
                .map(element -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("value", element);
                    result.put("matched", b.contains(element));
                    return result;
                })
                .collect(Collectors.toList());

        System.out.println("LEFT OUTER JOIN: " + leftOuterJoin);
        // [{value=1, matched=true}, {value=2, matched=true},
        // {value=3, matched=false}, {value=4, matched=false}]

        // LEFT OUTER JOIN con Optional
        List<Optional<? extends Object>> leftOuterJoinOptional = a.stream()
                .map(element -> b.contains(element) ? Optional.of(element) : Optional.empty())
                .collect(Collectors.toList());

        System.out.println("LEFT OUTER JOIN (Optional): " + leftOuterJoinOptional);
        // [Optional[1], Optional[2], Optional.empty, Optional.empty]

        /*
         * Observable.range(1, 5)
         * .map(numero -> numero * 10)
         * .subscribe(
         * resultado -> System.out.println("Resultado: " + resultado),
         * error -> System.err.println("Error: " + error),
         * () -> System.out.println("Completado")
         * );
         */

        Observable.just(1, 2, 0, 4)
                .concatMap(divisor -> Observable.just(divisor)
                        .map(d -> 10 / d)
                        .onErrorResumeNext(error -> {
                            System.err.println("Error con divisor " + divisor + ": " + error.getMessage());
                            return Observable.empty(); // Continúa sin emitir valor
                        }))
                .subscribe(
                        resultado -> System.out.println("Resultado: " + resultado),
                        error -> System.err.println("Error general: " + error),
                        () -> System.out.println("Completado"));

        Observable.just(1, 2, 0, 4)
                .map(divisor -> {
                    try {
                        return Optional.of(10 / divisor);
                    } catch (Exception e) {
                        System.err.println("Error con divisor " + divisor);
                        return Optional.<Integer>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribe(
                        resultado -> System.out.println("Resultado: " + resultado),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("Completado"));

        Observable.just(1, 2, 0, 4)
                .mapOptional(divisor -> {
                    try {
                        return Optional.of(10 / divisor);
                    } catch (Exception e) {
                        System.err.println("Error con divisor " + divisor);
                        return Optional.empty();
                    }
                })
                .subscribe(
                        resultado -> System.out.println("Resultado: " + resultado),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("Completado"));

        Observable.just(1, 2, 0, 4)
                .map(divisor -> {
                    try {
                        return 10 / divisor;
                    } catch (Exception e) {
                        System.err.println("Error con divisor " + divisor);
                        return null;
                    }
                })
                .filter(resultado -> resultado != null)
                .subscribe(
                        resultado -> System.out.println("Resultado: " + resultado),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("Completado"));

        Observable<Integer> left = Observable.just(1, 2, 3, 4);
        Observable<Integer> right = Observable.just(1, 3);

        System.out.println("LEFT OUTER JOIN");
        right.publish(rightShared -> left.flatMap(leftValue -> rightShared
                .filter(rightValue -> rightValue.equals(leftValue))
                .map(rightValue -> "Left: " + leftValue + " | Right: " + rightValue)
                .defaultIfEmpty("Left: " + leftValue + " | Right: NULL")))
                .subscribe(System.out::println);

        //

        System.out.println("NO ES INNJER JOIN");
        Observable.zip(left, right, (l, r) -> {
            return "Left: " + l + " | Right: " + r;
        })
        .subscribe(System.out::println);

        Observable<Integer> rightCached = right.replay().autoConnect();

        System.out.println("ES INNJER JOIN");
        left.flatMap(leftValue -> rightCached
                .filter(rightValue -> rightValue.equals(leftValue))
                .map(rightValue -> "Left: " + leftValue + " | Right: " + rightValue)).subscribe(System.out::println);

        Observable.just(1, 2, 3)
                .subscribeOn(Schedulers.io())
                .doOnNext(i -> System.out.println("Emit: " + Thread.currentThread().getName()))
                .observeOn(Schedulers.computation())
                .doOnNext(i -> System.out.println("Process: " + Thread.currentThread().getName()))
                .subscribe(i -> System.out.println("Subscribe: " + Thread.currentThread().getName()));
    }
}
