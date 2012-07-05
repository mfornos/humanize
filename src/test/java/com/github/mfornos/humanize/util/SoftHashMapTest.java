package com.github.mfornos.humanize.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.mfornos.humanize.util.SoftHashMap;

public class SoftHashMapTest {

	@Test
	public void SoftHashMap() {
		Map<String, Integer> m = new SoftHashMap<String, Integer>();
		Assert.assertNotNull(m);
		Integer num = new Integer(23);
		m.put("bla", num);
		Assert.assertEquals(m.get("bla"), num);
		Assert.assertEquals(m.size(), 1);
		m.remove("bla");
		Assert.assertTrue(m.isEmpty());

		for (int n = 0; n < 100; n++)
			m.put("bla" + n, n);

		Assert.assertEquals(m.size(), 100);

		for (Entry<String, Integer> e : m.entrySet()) {
			Assert.assertNotNull(e.getKey());
			Assert.assertNotNull(e.getValue());
			e.setValue(100);
			Assert.assertEquals(e.getValue(), new Integer(100));
		}

		Set<Entry<String, Integer>> eSet = m.entrySet();
		try {
			eSet.add(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.addAll(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.remove(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.removeAll(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.clear();
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.contains(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.containsAll(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}
		try {
			eSet.retainAll(null);
			Assert.fail();
		} catch (UnsupportedOperationException ex) {

		}

		Assert.assertFalse(eSet.isEmpty());
		Assert.assertNotNull(eSet.iterator().next());

		Assert.assertEquals(eSet.toArray().length, 100);
		Assert.assertEquals(eSet.toArray(new Map.Entry[] {}).length, 100);

		m.clear();

		Assert.assertTrue(m.isEmpty());
	}

	@Test
	public void SoftHashMapint() {
		Assert.assertNotNull(new SoftHashMap<String, Integer>(10));
	}

	@Test
	public void SoftHashMapintfloat() {
		Assert.assertNotNull(new SoftHashMap<String, Integer>(10, 1.5f));
	}

	@Test
	public void SoftHashMapMapextendsKextendsV() {
		Map<String, Integer> b = new HashMap<String, Integer>();
		b.put("1", 2);
		Map<String, Integer> m = new SoftHashMap<String, Integer>(b);
		Assert.assertEquals(m.get("1"), new Integer(2));
	}
}
