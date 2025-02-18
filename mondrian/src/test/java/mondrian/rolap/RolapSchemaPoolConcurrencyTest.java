/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2015-2017 Hitachi Vantara and others
// All Rights Reserved.
*/
package mondrian.rolap;

import static mondrian.rolap.RolapConnectionProperties.CatalogContent;
import static mondrian.rolap.RolapConnectionProperties.UseContentChecksum;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.daanse.engine.api.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import mondrian.olap.Util;
import mondrian.util.ByteString;

/**
 * @author Andrey Khayrutdinov
 */
@Disabled //has not been fixed during creating Daanse project
class RolapSchemaPoolConcurrencyTest
{

    private List<RolapSchema> addedSchemas;
    private RolapSchemaPool poolSpy;

    @BeforeEach
    public void beforeEach() {
        addedSchemas = new ArrayList<>();

        poolSpy = spy(RolapSchemaPool.instance());
        doAnswer(new RolapAnswer()).when(poolSpy)
                .createRolapSchema(
                    anyString(),
                    any(Context.class),
                    any(Util.PropertyList.class),
                    anyString(),
                    any(SchemaKey.class),
                    any(ByteString.class));
    }

    @AfterEach
    public void afterEach() {
        for (RolapSchema schema : addedSchemas) {
            RolapSchemaPool.instance().remove(schema);
        }
        addedSchemas = null;
        poolSpy = null;
    }


    class RolapAnswer implements Answer<RolapSchema>{
    @Override
    public RolapSchema answer(InvocationOnMock invocation) throws Throwable {
        SchemaKey key = (SchemaKey) invocation.getArguments()[4];
        ByteString md5 = (ByteString) invocation.getArguments()[5];
        RolapConnection connection = mock(RolapConnection.class);
        //noinspection deprecation
        return new RolapSchema(key, md5, connection);
    }

    }
    @Test
    void testTwentyAdders() throws Exception {
        final int cycles = 500;
        final int addersAmount = 10 * 2;

        List<Adder> adders = new ArrayList<>(addersAmount);
        for (int i = 0; i < addersAmount / 2; i++) {
            adders.add(new Adder(poolSpy, cycles, false));
            adders.add(new Adder(poolSpy, cycles, true));
        }

        try {
            runTest(adders);
        } finally {
            for (Adder adder : adders) {
                addedSchemas.addAll(adder.getAdded());
            }
        }
    }


    @Test
    void testTenAddersAndFiveRemovers() throws Exception {
        final int cycles = 200;
        final int removersAmount = 5;
        final int addersAmount = removersAmount * 2;

        List<Adder> adders = new ArrayList<>(addersAmount);
        List<Remover> removers = new ArrayList<>(removersAmount);
        for (int i = 0; i < removersAmount; i++) {
            BlockingQueue<RolapSchema> shared =
                new LinkedBlockingQueue<>();
            adders.add(new Adder(poolSpy, cycles, false, shared));
            adders.add(new Adder(poolSpy, cycles, true, shared));
            removers.add(new Remover(poolSpy, shared));
        }

        List<Callable<String>> actors =
            new ArrayList<>(addersAmount + removersAmount);
        actors.addAll(adders);
        actors.addAll(removers);
        Collections.shuffle(actors);

        try {
            runTest(actors);
        } finally {
            for (Adder adder : adders) {
                addedSchemas.addAll(adder.getAdded());
            }
        }
    }


    @Test
    void testTwentySimpleGetters() throws Exception {
        final int cycles = 1000;
        final int actorsAmount = 20;

        List<SingleSchemaGetter> actors =
            new ArrayList<>(actorsAmount);
        for (int i = 0; i < actorsAmount; i++) {
            String catalogUrl = UUID.randomUUID().toString();

            Context context = mock(Context.class);

            Util.PropertyList list = new Util.PropertyList();
            list.put(CatalogContent.name(), UUID.randomUUID().toString());

            // force the pool to create the fake schema
            RolapSchema schema = poolSpy.get(catalogUrl, context, list);
            addedSchemas.add(schema);

            actors.add(new SingleSchemaGetter(
                poolSpy, cycles, catalogUrl, context, list));
        }

        runTest(actors);
    }


    @Test
    void testFourAddersTwoRemoversTenGetters() throws Exception {
        final int addingCycles = 200;
        final int removersAmount = 2;
        final int addersAmount = removersAmount * 2;
        final int listingCycles = 500;
        final int gettersAmount = 10;

        List<Adder> adders = new ArrayList<>(addersAmount);
        List<Remover> removers = new ArrayList<>(removersAmount);
        for (int i = 0; i < removersAmount; i++) {
            BlockingQueue<RolapSchema> shared =
                new LinkedBlockingQueue<>();
            adders.add(new Adder(poolSpy, addingCycles, false, shared));
            adders.add(new Adder(poolSpy, addingCycles, true, shared));
            removers.add(new Remover(poolSpy, shared));
        }

        List<Getter> getters = new ArrayList<>(gettersAmount);
        for (int i = 0; i < gettersAmount; i++) {
            getters.add(new Getter(poolSpy, listingCycles));
        }

        List<Callable<String>> actors = new ArrayList<>(
            addersAmount + removersAmount);
        actors.addAll(adders);
        actors.addAll(removers);
        actors.addAll(getters);
        Collections.shuffle(actors);

        try {
            runTest(actors);
        } finally {
            for (Adder adder : adders) {
                addedSchemas.addAll(adder.getAdded());
            }
        }
    }


    private void runTest(final List<? extends Callable<String>> actors)
            throws Exception
    {
        List<String> errors = new ArrayList<>();
        ExecutorService executorService =
                Executors.newFixedThreadPool(actors.size());
        try {
            CompletionService<String> completionService =
                    new ExecutorCompletionService<>(executorService);
            for (Callable<String> reader : actors) {
                completionService.submit(reader);
            }

            for (int i = 0; i < actors.size(); i++) {
                Future<String> take = completionService.take();
                String result;
                try {
                    result = take.get();
                } catch (ExecutionException e) {
                    result = "Execution exception: " + e.getMessage();
                }
                if (result != null) {
                    errors.add(result);
                }
            }
        } finally {
            executorService.shutdown();
        }

        if (!errors.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("The following errors occurred: \n");
            for (String error : errors) {
                builder.append(error).append('\n');
            }
            fail(builder.toString());
        }
    }

    private static class Adder implements Callable<String> {
        private final RolapSchemaPool pool;
        private final int cycles;
        private final String catalogUrl;
        private final boolean needsCheckSum;
        private final Queue<RolapSchema> sharedQueue;

        private final List<RolapSchema> added;

        public Adder(RolapSchemaPool pool, int cycles, boolean needsCheckSum) {
            this(pool, cycles, needsCheckSum, null);
        }
        public Adder(
            RolapSchemaPool pool,
            int cycles,
            boolean needsCheckSum,
            Queue<RolapSchema> sharedQueue)
        {
            this.pool = pool;
            this.cycles = cycles;
            this.catalogUrl = "catalog";
            this.needsCheckSum = needsCheckSum;
            this.sharedQueue = sharedQueue;
            this.added = new ArrayList<>(cycles);
        }

        @Override
        public String call() throws Exception {
            Random random = new Random();
            for (int i = 0; i < cycles; i++) {
                Context context = mock(Context.class);

                Util.PropertyList list = new Util.PropertyList();
                list.put(CatalogContent.name(), UUID.randomUUID().toString());
                if (needsCheckSum) {
                    list.put(UseContentChecksum.name(), "true");
                }

                RolapSchema schema = pool.get(catalogUrl, context, list);
                added.add(schema);
                if (sharedQueue != null) {
                    sharedQueue.add(schema);
                }

                Thread.sleep(random.nextInt(50));
            }
            return null;
        }

        public List<RolapSchema> getAdded() {
            return added;
        }
    }

    private static class Remover implements Callable<String> {
        private final RolapSchemaPool pool;
        private final BlockingQueue<RolapSchema> sharedQueue;

        public Remover(
            RolapSchemaPool pool,
            BlockingQueue<RolapSchema> sharedQueue)
        {
            this.pool = pool;
            this.sharedQueue = sharedQueue;
        }

        @Override
        public String call() throws Exception {
            // sleep for a while to let adders do their work
            Thread.sleep(100);
            while (true) {
                RolapSchema schema = sharedQueue.poll(
                    250, TimeUnit.MILLISECONDS);
                if (schema == null) {
                    // let's give another chance
                    schema = sharedQueue.poll(1, TimeUnit.SECONDS);
                    if (schema == null) {
                        return null;
                    }
                }
                pool.remove(schema);
            }
        }
    }

    private static class Getter implements Callable<String> {
        private final RolapSchemaPool pool;
        private final int cycles;

        public Getter(RolapSchemaPool pool, int cycles) {
            this.pool = pool;
            this.cycles = cycles;
        }

        @Override
        public String call() throws Exception {
            Random random = new Random();
            for (int i = 0; i < cycles; i++) {
                int acc = 0;
                for (RolapSchema schema : pool.getRolapSchemas()) {
                    // fake actions to prevent JIT from eliminating this block
                    acc += schema.key.hashCode();
                }
                if (acc < 0) {
                    acc = -acc;
                }
                Thread.sleep(Math.min(random.nextInt(50), acc));
            }
            return null;
        }
    }

    private static class SingleSchemaGetter implements Callable<String> {
        private final RolapSchemaPool pool;
        private final int cycles;
        private final String catalogUrl;
        private final Context context;
        private final Util.PropertyList list;

        public SingleSchemaGetter(
            RolapSchemaPool pool,
            int cycles,
            String catalogUrl,
            Context context,
            Util.PropertyList list)
        {
            this.pool = pool;
            this.cycles = cycles;
            this.catalogUrl = catalogUrl;
            this.context = context;
            this.list = list;
        }

        @Override
        public String call() throws Exception {
            for (int i = 0; i < cycles; i++) {
                RolapSchema schema = pool.get(catalogUrl, context, list);
                assertNotNull(schema,
            		String.format(
                    "Catalog: [%s], catalog content: [%s]", catalogUrl,
                    list.get(CatalogContent.name())));
            }
            return null;
        }
    }
}
