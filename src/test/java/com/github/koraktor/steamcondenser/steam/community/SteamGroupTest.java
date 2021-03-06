/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.steamcondenser.steam.community;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.w3c.dom.Document;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
public class SteamGroupTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DocumentBuilder parser;

    @Before
    public void setup() throws Exception {
        this.parser = mock(DocumentBuilder.class);

        XMLData.setDocumentBuilder(this.parser);
    }

    @Test
    public void testCache() throws SteamCondenserException {
        assertFalse(SteamGroup.isCached(103582791429521412L));

        SteamGroup group = new SteamGroup(103582791429521412L, false);

        assertTrue(group.cache());
        assertTrue(SteamGroup.isCached(103582791429521412L));
        assertFalse(group.cache());
    }

    @Test
    public void testCacheWithCustomUrl() throws SteamCondenserException {
        assertFalse(SteamGroup.isCached("valve"));

        SteamGroup group = new SteamGroup("valve", false);

        assertTrue(group.cache());
        assertTrue(SteamGroup.isCached("valve"));
        assertFalse(group.cache());
    }

    @Test
    public void testFetchMembers() throws Exception {
        Document memberDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.getClass().getResourceAsStream("valve-members.xml"));
        when(this.parser.parse("http://steamcommunity.com/groups/valve/memberslistxml?p=1")).thenReturn(memberDocument);

        SteamGroup group = new SteamGroup("valve", true);
        ArrayList<SteamId> members = group.getMembers();

        assertThat(group.getMemberCount(), is(221));
        assertThat(members.get(0).getSteamId64(), is(76561197960265740L));
        assertFalse(members.get(0).isFetched());
        assertThat(members.get(members.size() - 1).getSteamId64(), is(76561197970323416L));
        assertTrue(group.isFetched());

        verify(this.parser).parse("http://steamcommunity.com/groups/valve/memberslistxml?p=1");
    }

    @Test
    public void testGroupByGroupId64() throws SteamCondenserException {
        SteamGroup group = new SteamGroup(103582791429521412L, false);

        assertThat(group.getGroupId64(), is(103582791429521412L));
        assertThat(group.getBaseUrl(), is(equalTo("http://steamcommunity.com/gid/103582791429521412")));
    }

    @Test
    public void testGroupByCustomUrl() throws SteamCondenserException {
        SteamGroup group = new SteamGroup("valve", false);

        assertThat(group.getCustomUrl(), is("valve"));
        assertThat(group.getBaseUrl(), is(equalTo("http://steamcommunity.com/groups/valve")));
    }

    @Test
    public void testInvalidXml() throws Exception {
        this.expectedException.expect(is(instanceOf(SteamCondenserException.class)));
        this.expectedException.expectMessage("XML data could not be parsed.");

        Document memberDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.getClass().getResourceAsStream("invalid.xml"));
        when(this.parser.parse("http://steamcommunity.com/groups/valve/memberslistxml?p=1")).thenReturn(memberDocument);

        SteamGroup group = new SteamGroup("valve", true);
        ArrayList<SteamId> members = group.getMembers();

        assertThat(group.getMemberCount(), is(221));
        assertThat(members.get(0).getSteamId64(), is(76561197960265740L));
        assertFalse(members.get(0).isFetched());
        assertThat(members.get(members.size() - 1).getSteamId64(), is(76561197970323416L));
        assertTrue(group.isFetched());

        verify(this.parser).parse("http://steamcommunity.com/groups/valve/memberslistxml?p=1");
    }

    @Test
    public void testMemberCount() throws Exception {
        Document memberDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.getClass().getResourceAsStream("valve-members.xml"));
        when(this.parser.parse("http://steamcommunity.com/groups/valve/memberslistxml?p=1")).thenReturn(memberDocument);

        SteamGroup group = new SteamGroup("valve", false);
        assertThat(group.getMemberCount(), is(221));

        verify(this.parser).parse("http://steamcommunity.com/groups/valve/memberslistxml?p=1");
    }

    @After
    public void teardown() {
        SteamGroup.steamGroups.clear();
    }

}
