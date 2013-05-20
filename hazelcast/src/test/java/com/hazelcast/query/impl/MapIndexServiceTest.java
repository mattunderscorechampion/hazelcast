/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.query.impl;

import com.hazelcast.query.EntryObject;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.test.RandomBlockJUnit4ClassRunner;
import com.hazelcast.util.Clock;
import com.hazelcast.instance.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class MapIndexServiceTest extends TestUtil {

    @Test
    public void testAndWithSingleEntry() throws Exception {
        IndexService mapIndexService = new IndexService();
        mapIndexService.addOrGetIndex("name", false);
        mapIndexService.addOrGetIndex("age", true);
        mapIndexService.addOrGetIndex("salary", true);
        for (int i = 0; i < 20000; i++) {
            Employee employee = new Employee(i + "Name", i % 80, (i % 2 == 0), 100 + (i % 1000));
            mapIndexService.saveEntryIndex(new QueryEntry(null, toData(i), i, employee));
        }
        int count = 1000;
        Set<String> ages = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            ages.add(String.valueOf(i));
        }
        final EntryObject entryObject = new PredicateBuilder().getEntryObject();
        final PredicateBuilder predicate = entryObject.get("name").equal("140Name").and(entryObject.get("age").in(ages.toArray(new String[0])));
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        System.out.println("Used Memory:" + ((total - free) / 1024 / 1024));
        long start = Clock.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Set<QueryableEntry> results = mapIndexService.query(predicate);
            assertEquals(1, results.size());
        }
        System.out.println("Took " + (Clock.currentTimeMillis() - start));
    }

    @Test
    public void testIndex() throws Exception {
        IndexService indexService = new IndexService();
        indexService.addOrGetIndex("name", false);
        indexService.addOrGetIndex("age", true);
        indexService.addOrGetIndex("salary", true);
        for (int i = 0; i < 20000; i++) {
            Employee employee = new Employee(i + "Name", i % 80, (i % 2 == 0), 100 + (i % 1000));
            indexService.saveEntryIndex(new QueryEntry(null, toData(i), i, employee));
        }
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        System.out.println("Used Memory:" + ((total - free) / 1024 / 1024));
        long start = Clock.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            SqlPredicate predicate = new SqlPredicate("salary=161 and age >20 and age <23");
            Set<QueryableEntry> results = new HashSet<QueryableEntry>(indexService.query(predicate));
            assertEquals(10, results.size());
        }
        System.out.println("Took " + (Clock.currentTimeMillis() - start));
    }

    @Test
    public void testIndex2() throws Exception {
        IndexService indexService = new IndexService();
        indexService.addOrGetIndex("name", false);
        indexService.saveEntryIndex(new QueryEntry(null, toData(1), 1, new Value("abc")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(2), 2, new Value("xyz")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(3), 3, new Value("aaa")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(4), 4, new Value("zzz")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(5), 5, new Value("klm")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(6), 6, new Value("prs")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(7), 7, new Value("prs")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(8), 8, new Value("def")));
        indexService.saveEntryIndex(new QueryEntry(null, toData(9), 9, new Value("qwx")));
        assertEquals(8, new HashSet(indexService.query(new SqlPredicate("name > 'aac'"))).size());
    }
}