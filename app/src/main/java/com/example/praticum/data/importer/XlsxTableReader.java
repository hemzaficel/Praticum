package com.example.praticum.data.importer;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public final class XlsxTableReader {
    private XlsxTableReader() {
    }

    public static List<List<String>> read(InputStream in) throws Exception {
        Map<String, byte[]> z = new HashMap<>();
        try (ZipInputStream x = new ZipInputStream(in)) {
            ZipEntry e;
            byte[] b = new byte[8192];
            while ((e = x.getNextEntry()) != null) if (!e.isDirectory()) {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                int n;
                while ((n = x.read(b)) > 0) o.write(b, 0, n);
                z.put(e.getName(), o.toByteArray());
            }
        }
        byte[] s = z.get("xl/worksheets/sheet1.xml");
        if (s == null) throw new IOException("Feuille Excel introuvable.");
        return sheet(s, strings(z.get("xl/sharedStrings.xml")));
    }

    private static List<String> strings(byte[] b) throws Exception {
        List<String> r = new ArrayList<>();
        if (b == null) return r;
        XmlPullParser p = Xml.newPullParser();
        p.setInput(new ByteArrayInputStream(b), "UTF-8");
        StringBuilder q = null;
        while (p.next() != XmlPullParser.END_DOCUMENT) {
            if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equals("si"))
                q = new StringBuilder();
            else if (p.getEventType() == XmlPullParser.START_TAG && p.getName().equals("t") && q != null)
                q.append(p.nextText());
            else if (p.getEventType() == XmlPullParser.END_TAG && p.getName().equals("si") && q != null) {
                r.add(q.toString());
                q = null;
            }
        }
        return r;
    }

    private static List<List<String>> sheet(byte[] b, List<String> shared) throws Exception {
        List<List<String>> rows = new ArrayList<>();
        XmlPullParser p = Xml.newPullParser();
        p.setInput(new ByteArrayInputStream(b), "UTF-8");
        List<String> row = null;
        String type = "";
        int col = 0;
        StringBuilder val = null;
        while (p.next() != XmlPullParser.END_DOCUMENT) {
            int ev = p.getEventType();
            if (ev == XmlPullParser.START_TAG) {
                String t = p.getName();
                if (t.equals("row")) row = new ArrayList<>();
                else if (t.equals("c") && row != null) {
                    String ref = p.getAttributeValue(null, "r");
                    col = column(ref);
                    type = p.getAttributeValue(null, "t");
                    val = new StringBuilder();
                } else if ((t.equals("v") || t.equals("t")) && val != null)
                    val.append(p.nextText());
            } else if (ev == XmlPullParser.END_TAG) {
                String t = p.getName();
                if (t.equals("c") && row != null && val != null) {
                    while (row.size() <= col) row.add("");
                    String v = val.toString();
                    if ("s".equals(type)) try {
                        v = shared.get(Integer.parseInt(v));
                    } catch (Exception ignored) {
                        v = "";
                    }
                    row.set(col, v);
                    val = null;
                } else if (t.equals("row") && row != null) {
                    rows.add(row);
                    row = null;
                }
            }
        }
        return rows;
    }

    private static int column(String ref) {
        int n = 0;
        if (ref != null) for (int i = 0; i < ref.length() && Character.isLetter(ref.charAt(i)); i++)
            n = n * 26 + Character.toUpperCase(ref.charAt(i)) - 'A' + 1;
        return Math.max(0, n - 1);
    }
}