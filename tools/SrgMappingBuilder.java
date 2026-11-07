
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Builds an "official -> SRG" TSRG mapping so a jar compiled against Mojang official
 * names can be reobfuscated into the NeoForge/Forge 1.20.1 production namespace
 * (official class names + SRG member names).
 *
 *   official side   <- proguard client.txt / server.txt   (Mojang, "<official> -> <obf>:")
 *   obf -> srg side <- build/neoForm/.../steps/mergeMappings/output.tsrg (tsrg)
 *
 * Methods join on (obf name, obf descriptor); tsrg field lines carry no descriptor, so
 * fields join on the obf field name, which is unique per class in the Minecraft obf jar.
 * Class names are identity-mapped: only member names change.
 * Finally the mod jar is walked so its own methods that override Minecraft members get
 * renamed too (the JVM dispatches on name+descriptor, so declarations must follow).
 */
public final class SrgMappingBuilder {

    static final HashMap<String, String> officialToObf = new HashMap<>();   // dotted official -> obf
    static final HashMap<String, String> obfToOfficial = new HashMap<>();   // obf -> slashed official
    static final HashMap<String, HashMap<String, String>> pgMethod = new HashMap<>(); // slashed official -> (obfName obfDesc) -> officialName
    static final HashMap<String, HashMap<String, String>> pgField = new HashMap<>();  // slashed official -> obfName -> officialName

    static final String AMBIG = "\u0000AMBIG";
    // slashed official class -> member key -> srg name.
    // member key = "name desc" for methods, plain "name" for fields.
    static final TreeMap<String, HashMap<String, String>> out = new TreeMap<>();

    static int pgClasses = 0, pgMeth = 0, pgFieldCount = 0;
    static int tsrgClasses = 0, tsrgMembers = 0;
    static int joinedMeth = 0, joinedField = 0, missingMeth = 0, missingField = 0, ambigField = 0;
    static int identitySkip = 0, conflicts = 0, hierAdded = 0, hierRefsResolved = 0, hierRefsSeen = 0;
    static final ArrayList<String> conflictSamples = new ArrayList<>();
    static final ArrayList<String> missingSamples = new ArrayList<>();

    static String toSlash(String s) { return s.replace('.', '/'); }

    static String typeToDesc(String t) {
        int dims = 0;
        while (t.endsWith("[]")) { dims++; t = t.substring(0, t.length() - 2); }
        String base;
        switch (t) {
            case "void": base = "V"; break;
            case "int": base = "I"; break;
            case "long": base = "J"; break;
            case "boolean": base = "Z"; break;
            case "byte": base = "B"; break;
            case "char": base = "C"; break;
            case "short": base = "S"; break;
            case "float": base = "F"; break;
            case "double": base = "D"; break;
            default: base = "L" + officialToObf.getOrDefault(t, t).replace('.', '/') + ";";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dims; i++) sb.append('[');
        return sb.append(base).toString();
    }

    static String translateDesc(String d, Map<String, String> cls) {
        StringBuilder sb = new StringBuilder(d.length() + 16);
        int i = 0;
        while (i < d.length()) {
            char c = d.charAt(i);
            if (c == 'L') {
                int end = d.indexOf(';', i);
                if (end < 0) { sb.append(d, i, d.length()); break; }
                String n = d.substring(i + 1, end);
                sb.append('L').append(cls.getOrDefault(n, n)).append(';');
                i = end + 1;
            } else { sb.append(c); i++; }
        }
        return sb.toString();
    }

    // ---------------- proguard ----------------

    static void parseProguard(Path p, boolean classPass) throws IOException {
        String curOfficial = null;
        HashMap<String, String> curM = null, curF = null;
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String raw;
            while ((raw = r.readLine()) != null) {
                if (raw.isEmpty()) continue;
                char c0 = raw.charAt(0);
                if (c0 != ' ' && c0 != '\t') {
                    int arrow = raw.indexOf(" -> ");
                    if (arrow < 0) { curOfficial = null; curM = null; curF = null; continue; }
                    String left = raw.substring(0, arrow).trim();
                    String right = raw.substring(arrow + 4).trim();
                    int colon = right.indexOf(':');
                    if (colon >= 0) right = right.substring(0, colon);
                    curOfficial = toSlash(left);
                    officialToObf.putIfAbsent(left, right);
                    obfToOfficial.putIfAbsent(right, curOfficial);
                    pgClasses++;
                    if (!classPass) {
                        curM = pgMethod.computeIfAbsent(curOfficial, k -> new HashMap<>());
                        curF = pgField.computeIfAbsent(curOfficial, k -> new HashMap<>());
                    }
                    continue;
                }
                if (classPass || curM == null) continue;
                String line = raw.trim();
                int arrow = line.lastIndexOf(" -> ");
                if (arrow < 0) continue;
                String left = line.substring(0, arrow).trim();
                String obfName = line.substring(arrow + 4).trim();
                int colon = obfName.indexOf(':');
                if (colon >= 0) obfName = obfName.substring(0, colon);
                int paren = left.indexOf('(');
                try {
                    if (paren >= 0) {
                        int close = left.lastIndexOf(')');
                        if (close < 0) continue;
                        String sig = left.substring(paren + 1, close);
                        String head = left.substring(0, paren).trim();
                        int sp = head.lastIndexOf(' ');
                        if (sp < 0) continue;
                        String retType = head.substring(0, sp).trim().replaceFirst("^\\d+:\\d+:", "");
                        String officialName = head.substring(sp + 1).trim();
                        StringBuilder desc = new StringBuilder("(");
                        if (!sig.isEmpty()) {
                            for (String pt : sig.split(",")) desc.append(typeToDesc(pt.trim()));
                        }
                        desc.append(')').append(typeToDesc(retType));
                        curM.putIfAbsent(obfName + " " + desc, officialName);
                        pgMeth++;
                    } else {
                        int sp = left.lastIndexOf(' ');
                        if (sp < 0) continue;
                        String type = left.substring(0, sp).trim();
                        String officialName = left.substring(sp + 1).trim();
                        String prev = curF.get(obfName);
                        if (prev == null) curF.put(obfName, officialName);
                        else if (!prev.equals(officialName)) curF.put(obfName, AMBIG);
                        pgFieldCount++;
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    // ---------------- tsrg ----------------

    static void parseTsrg(Path p) throws IOException {
        String obfClass = null, officialClass = null;
        boolean tsrg2 = false;
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String raw;
            while ((raw = r.readLine()) != null) {
                if (raw.isEmpty()) continue;
                if (raw.startsWith("tsrg2")) { tsrg2 = true; continue; }
                if (raw.startsWith("#")) continue;
                int indent = 0;
                while (indent < raw.length() && raw.charAt(indent) == '\t') indent++;
                String line = raw.substring(indent);
                if (line.isEmpty()) continue;
                String[] t = line.split("\\s+");
                if (indent == 0) {
                    if (t.length < 2) continue;
                    obfClass = t[0];
                    officialClass = t[1];
                    tsrgClasses++;
                    continue;
                }
                if (indent != 1 || obfClass == null) continue;
                if (t.length < 2) continue;
                tsrgMembers++;
                String obfName = t[0];
                boolean isMethod = t[1].startsWith("(");
                String srgName, key, officialMember, officialDesc = null;
                if (isMethod) {
                    if (t.length >= 3) { officialDesc = t[1]; srgName = t[2]; }
                    else {
                        int close = t[1].lastIndexOf(')');
                        if (close < 0) continue;
                        officialDesc = t[1].substring(0, close + 1);
                        srgName = t[1].substring(close + 1);
                    }
                } else {
                    srgName = tsrg2 && t.length >= 3 ? t[1] : t[t.length - 1];
                }
                HashMap<String, String> src = isMethod ? pgMethod.get(officialClass) : pgField.get(officialClass);
                if (isMethod) {
                    key = obfName + " " + officialDesc;
                    officialMember = src == null ? null : src.get(key);
                } else {
                    key = obfName;
                    officialMember = src == null ? null : src.get(key);
                    if (AMBIG.equals(officialMember)) {
                        officialMember = null;
                        ambigField++;
                    }
                }
                if (officialMember == null) {
                    if (isMethod) {
                        missingMeth++;
                        if (missingSamples.size() < 20)
                            missingSamples.add("METHOD " + officialClass + " " + key + " -> " + srgName);
                    } else {
                        missingField++;
                        if (missingSamples.size() < 20)
                            missingSamples.add("FIELD  " + officialClass + " " + key + " -> " + srgName);
                    }
                    continue;
                }
                if (officialMember.equals(srgName)) { identitySkip++; continue; }
                String outKey;
                if (isMethod) {
                    officialDesc = translateDesc(officialDesc, obfToOfficial);
                    outKey = officialMember + " " + officialDesc;
                } else {
                    outKey = officialMember;
                }
                HashMap<String, String> cls = out.computeIfAbsent(officialClass, k -> new HashMap<>());
                String prev = cls.get(outKey);
                if (prev != null && !prev.equals(srgName)) {
                    conflicts++;
                    if (conflictSamples.size() < 20)
                        conflictSamples.add(officialClass + " " + outKey + " : " + prev + " vs " + srgName);
                    continue;
                }
                cls.put(outKey, srgName);
                if (isMethod) joinedMeth++; else joinedField++;
            }
        }
    }

    // ---------------- class file parsing ----------------

    static final class Cls {
        String name, superName;
        String[] interfaces = new String[0];
        Set<String> methods = new LinkedHashSet<>();
        Set<String> refs = new LinkedHashSet<>();
    }

    static Cls readClass(byte[] b, boolean collectRefs) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
        if (in.readInt() != 0xCAFEBABE) throw new IOException("bad magic");
        in.readUnsignedShort(); in.readUnsignedShort();
        int cpCount = in.readUnsignedShort();
        Object[] cp = new Object[cpCount];
        byte[] cpTag = new byte[cpCount];
        for (int i = 1; i < cpCount; i++) {
            int t = in.readUnsignedByte();
            cpTag[i] = (byte) t;
            switch (t) {
                case 1: cp[i] = in.readUTF(); break;
                case 3: case 4: in.skipBytes(4); break;
                case 5: case 6: in.skipBytes(8); i++; break;
                case 7: case 8: case 16: case 19: case 20: cp[i] = in.readUnsignedShort(); break;
                case 9: case 10: case 11:
                case 12: case 17: case 18:
                    cp[i] = new int[]{in.readUnsignedShort(), in.readUnsignedShort()};
                    break;
                case 15: in.skipBytes(3); break;
                default: throw new IOException("bad cp tag " + t);
            }
        }
        Cls c = new Cls();
        in.readUnsignedShort();
        int thisClass = in.readUnsignedShort();
        c.name = (String) cp[((Integer) cp[thisClass]) & 0xFFFF];
        int sup = in.readUnsignedShort();
        if (sup != 0) c.superName = (String) cp[((Integer) cp[sup]) & 0xFFFF];
        int ni = in.readUnsignedShort();
        c.interfaces = new String[ni];
        for (int i = 0; i < ni; i++) {
            int idx = in.readUnsignedShort();
            c.interfaces[i] = (String) cp[((Integer) cp[idx]) & 0xFFFF];
        }
        for (int pass = 0; pass < 2; pass++) {
            int n = in.readUnsignedShort();
            for (int i = 0; i < n; i++) {
                in.readUnsignedShort();
                String nm = (String) cp[in.readUnsignedShort()];
                String ds = (String) cp[in.readUnsignedShort()];
                if (pass == 1) c.methods.add(nm + " " + ds);
                int attrs = in.readUnsignedShort();
                for (int a = 0; a < attrs; a++) { in.readUnsignedShort(); in.skipBytes(in.readInt()); }
            }
        }
        int attrs = in.readUnsignedShort();
        for (int a = 0; a < attrs; a++) { in.readUnsignedShort(); in.skipBytes(in.readInt()); }

        if (collectRefs) {
            for (int i = 1; i < cpCount; i++) {
                int t = cpTag[i];
                if (t != 9 && t != 10 && t != 11) continue;
                int[] pair = (int[]) cp[i];
                Object cls = cp[pair[0]], nat = cp[pair[1]];
                if (!(cls instanceof Integer) || !(nat instanceof int[])) continue;
                Object owner = cp[(Integer) cls];
                if (!(owner instanceof String)) continue;
                int[] nt = (int[]) nat;
                Object nm = cp[nt[0]], ds = cp[nt[1]];
                if (!(nm instanceof String) || !(ds instanceof String)) continue;
                c.refs.add(owner + "#" + (t == 9 ? "f" : "m") + nm + " " + ds);
            }
        }
        return c;
    }

    // ---------------- main ----------------

    static String lookup(String cls, String key) {
        HashMap<String, String> m = out.get(cls);
        return m == null ? null : m.get(key);
    }

    static String resolveHier(String cls, String key, Map<String, Cls> info) {
        ArrayDeque<String> q = new ArrayDeque<>();
        HashSet<String> seen = new HashSet<>();
        q.add(cls);
        while (!q.isEmpty()) {
            String c = q.poll();
            if (!seen.add(c)) continue;
            String s = lookup(c, key);
            if (s != null) return s;
            Cls k = info.get(c);
            if (k != null) {
                if (k.superName != null) q.add(k.superName);
                for (String itf : k.interfaces) q.add(itf);
            }
        }
        return null;
    }

    static boolean isSrgName(String n) {
        if (n.length() < 4) return false;
        char c0 = n.charAt(0), c1 = n.charAt(1);
        if (!((c0 == 'm' || c0 == 'f') && c1 == '_')) return false;
        if (n.charAt(n.length() - 1) != '_') return false;
        for (int i = 2; i < n.length() - 1; i++) if (!Character.isDigit(n.charAt(i))) return false;
        return true;
    }

    /** counts members of the given jar that still carry an official name although the mapping renames them */
    static int checkJar(Path jar, Map<String, Cls> info, ArrayList<String> bad, ArrayList<String> decl) {
        int count = 0;
        try (ZipFile z = new ZipFile(jar.toFile())) {
            for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements(); ) {
                ZipEntry ze = e.nextElement();
                if (!ze.getName().endsWith(".class")) continue;
                Cls c;
                try { c = readClass(readAll(z, ze), true); } catch (IOException ex) { continue; }
                if (c.name == null) continue;
                for (String key : c.methods) {
                    int sp = key.indexOf(' ');
                    String n = sp < 0 ? key : key.substring(0, sp);
                    if (isSrgName(n) || n.startsWith("<") || n.startsWith("lambda$")) continue;
                    String m = resolveHier(c.name, key, info);
                    if (m != null && !m.equals(n)) {
                        count++;
                        if (decl.size() < 12) decl.add(c.name.replace('/', '.') + "." + key + " (declared, expected " + m + ")");
                    }
                }
                for (String r : c.refs) {
                    int hash = r.indexOf('#');
                    if (hash < 0) continue;
                    String owner = r.substring(0, hash);
                    String rest = r.substring(hash + 1);
                    char kind = rest.charAt(0);
                    String nmDs = rest.substring(1);
                    int sp = nmDs.indexOf(' ');
                    String n = sp < 0 ? nmDs : nmDs.substring(0, sp);
                    if (isSrgName(n) || n.startsWith("<") || n.startsWith("lambda$")) continue;
                    String key = kind == 'f' ? n : nmDs;
                    String m = resolveHier(owner, key, info);
                    if (m != null && !m.equals(n)) {
                        count++;
                        if (bad.size() < 15) bad.add(owner.replace('/', '.') + "." + key + " -> expected " + m);
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        Path pgClient = null, pgServer = null, tsrg = null, mcJar = null, modJar = null, outFile = null, report = null, checkJar = null;
        List<Path> extra = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--proguard-client": pgClient = Paths.get(args[++i]); break;
                case "--proguard-server": pgServer = Paths.get(args[++i]); break;
                case "--tsrg": tsrg = Paths.get(args[++i]); break;
                case "--mc-jar": mcJar = Paths.get(args[++i]); break;
                case "--mod-jar": modJar = Paths.get(args[++i]); break;
                case "--extra-jar": extra.add(Paths.get(args[++i])); break;
                case "--out": outFile = Paths.get(args[++i]); break;
                case "--report": report = Paths.get(args[++i]); break;
                case "--check-jar": checkJar = Paths.get(args[++i]); break;
                default: throw new IllegalArgumentException("unknown arg " + args[i]);
            }
        }

        long t0 = System.currentTimeMillis();
        parseProguard(pgClient, true);
        if (pgServer != null && Files.exists(pgServer)) parseProguard(pgServer, true);
        long t1 = System.currentTimeMillis();
        parseProguard(pgClient, false);
        if (pgServer != null && Files.exists(pgServer)) parseProguard(pgServer, false);
        long t2 = System.currentTimeMillis();
        parseTsrg(tsrg);
        long t3 = System.currentTimeMillis();

        Map<String, Cls> info = new HashMap<>();
        int mcClasses = 0, extraClasses = 0, modClasses = 0;
        ArrayList<Cls> modClses = new ArrayList<>();
        try (ZipFile z = new ZipFile(mcJar.toFile())) {
            for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements(); ) {
                ZipEntry ze = e.nextElement();
                if (!ze.getName().endsWith(".class")) continue;
                Cls c = readClass(readAll(z, ze), false);
                if (c.name == null) continue;
                info.put(c.name, c);
                mcClasses++;
            }
        }
        for (Path p : extra) {
            if (!Files.exists(p)) continue;
            try (ZipFile z = new ZipFile(p.toFile())) {
                for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements(); ) {
                    ZipEntry ze = e.nextElement();
                    if (!ze.getName().endsWith(".class")) continue;
                    Cls c = readClass(readAll(z, ze), false);
                    if (c.name == null || info.containsKey(c.name)) continue;
                    info.put(c.name, c);
                    extraClasses++;
                }
            }
        }
        try (ZipFile z = new ZipFile(modJar.toFile())) {
            for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements(); ) {
                ZipEntry ze = e.nextElement();
                if (!ze.getName().endsWith(".class")) continue;
                Cls c = readClass(readAll(z, ze), true);
                if (c.name == null) continue;
                info.putIfAbsent(c.name, c);
                modClses.add(c);
                modClasses++;
            }
        }
        // (a) declarations inside the mod's own classes that override a Minecraft member
        for (Cls c : modClses) {
            HashMap<String, String> mine = out.computeIfAbsent(c.name, k -> new HashMap<>());
            for (String key : new LinkedHashSet<>(c.methods)) {
                if (mine.containsKey(key)) continue;
                String s = resolveHier(c.name, key, info);
                int sp = key.indexOf(' ');
                if (s != null && !s.equals(sp < 0 ? key : key.substring(0, sp))) { mine.put(key, s); hierAdded++; }
            }
        }
        // (b) references whose owner class does not declare the member (inherited call sites)
        int refsSeen = 0, refsHier = 0;
        ArrayList<String> refSamples = new ArrayList<>();
        for (Cls c : modClses) {
            for (String r : c.refs) {
                int hash = r.indexOf('#');
                if (hash < 0) continue;
                String owner = r.substring(0, hash);
                String rest = r.substring(hash + 1);
                char kind = rest.charAt(0);
                String nmDs = rest.substring(1);
                int sp = nmDs.indexOf(' ');
                String nm = sp < 0 ? nmDs : nmDs.substring(0, sp);
                String key = kind == 'f' ? nm : nmDs;
                refsSeen++;
                if (lookup(owner, key) != null) continue;
                HashMap<String, String> mine = out.computeIfAbsent(owner, k -> new HashMap<>());
                if (mine.containsKey(key)) continue;
                String s = resolveHier(owner, key, info);
                if (s != null && !s.equals(nm)) {
                    mine.put(key, s); hierAdded++; refsHier++;
                    if (refSamples.size() < 10) refSamples.add(owner + " " + key + " -> " + s);
                }
            }
        }
        hierRefsResolved = refsHier;
        hierRefsSeen = refsSeen;
        long t4 = System.currentTimeMillis();

        Files.createDirectories(outFile.toAbsolutePath().getParent());
        int lines = 0, classesWritten = 0;
        try (BufferedWriter w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, HashMap<String, String>> e : out.entrySet()) {
                HashMap<String, String> m = e.getValue();
                if (m.isEmpty()) continue;
                ArrayList<String> keys = new ArrayList<>(m.keySet());
                Collections.sort(keys);
                w.write(e.getKey()); w.write(' '); w.write(e.getKey()); w.write('\n');
                lines++; classesWritten++;
                for (String key : keys) {
                    int sp = key.indexOf(' ');
                    w.write('\t');
                    if (sp < 0) {                       // field: no descriptor in tsrg
                        w.write(key); w.write(' '); w.write(m.get(key)); w.write('\n');
                    } else {                            // method: name desc newname
                        w.write(key, 0, sp); w.write(' ');
                        w.write(key, sp + 1, key.length() - sp - 1); w.write(' ');
                        w.write(m.get(key)); w.write('\n');
                    }
                    lines++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SrgMappingBuilder report\n");
        String[] heads = {"proguard client", "proguard server", "tsrg", "mc srg jar", "mod jar", "output mapping"};
        Object[] vals = {pgClient, pgServer, tsrg, mcJar, modJar, outFile};
        for (int i = 0; i < heads.length; i++) sb.append("  ").append(pad(heads[i])).append(": ").append(vals[i]).append('\n');
        sb.append("  ").append(pad("proguard classes")).append(": ").append(pgClasses).append('\n');
        sb.append("  ").append(pad("proguard members")).append(": ").append(pgMeth).append(" methods, ").append(pgFieldCount).append(" fields\n");
        sb.append("  ").append(pad("tsrg classes")).append(": ").append(tsrgClasses).append('\n');
        sb.append("  ").append(pad("tsrg member lines")).append(": ").append(tsrgMembers).append('\n');
        sb.append("  ").append(pad("joined methods")).append(": ").append(joinedMeth).append('\n');
        sb.append("  ").append(pad("joined fields")).append(": ").append(joinedField).append('\n');
        sb.append("  ").append(pad("identity (no rename)")).append(": ").append(identitySkip).append('\n');
        sb.append("  ").append(pad("unmatched methods")).append(": ").append(missingMeth).append('\n');
        sb.append("  ").append(pad("unmatched fields")).append(": ").append(missingField).append(" (ambiguous: ").append(ambigField).append(")\n");
        sb.append("  ").append(pad("conflicts")).append(": ").append(conflicts).append('\n');
        sb.append("  ").append(pad("classes mc / extra / mod")).append(": ").append(mcClasses).append(" / ").append(extraClasses).append(" / ").append(modClasses).append('\n');
        sb.append("  ").append(pad("override entries added")).append(": ").append(hierAdded).append('\n');
        sb.append("  ").append(pad("member refs scanned")).append(": ").append(hierRefsSeen).append("\n");
        sb.append("  ").append(pad("refs via hierarchy")).append(": ").append(hierRefsResolved).append('\n');
        sb.append("  ").append(pad("mapping classes / lines")).append(": ").append(classesWritten).append(" / ").append(lines).append('\n');
        sb.append("  ").append(pad("timings ms")).append(": proguard=").append(t1 - t0).append(" members=").append(t2 - t1)
          .append(" tsrg=").append(t3 - t2).append(" hierarchy=").append(t4 - t3).append('\n');

        String[][] spots = {
                {"net/minecraft/util/StringRepresentable", "fromEnum"},
                {"net/minecraft/util/StringRepresentable", "getSerializedName"},
                {"net/minecraft/world/item/ItemStack", "shrink"},
                {"net/minecraft/world/level/block/state/BlockState", "setValue"},
                {"net/minecraft/world/item/ItemStack", "isEmpty"},
        };
        sb.append("\nSPOT CHECKS (official -> srg)\n");
        for (String[] s : spots) {
            HashMap<String, String> m = out.get(s[0]);
            boolean any = false;
            if (m != null) {
                ArrayList<String> keys = new ArrayList<>(m.keySet());
                Collections.sort(keys);
                for (String k : keys) {
                    if (k.startsWith(s[1] + " ") || k.equals(s[1])) {
                        sb.append("  ").append(s[0].replace('/', '.')).append('.').append(k).append("  ->  ").append(m.get(k)).append('\n');
                        any = true;
                    }
                }
            }
            if (!any) sb.append("  ").append(s[0]).append('.').append(s[1]).append("  ->  (not found)\n");
        }

        sb.append("\nsample override entries for the mod's own classes\n");
        int shown = 0;
        for (Cls c : modClses) {
            HashMap<String, String> m = out.get(c.name);
            if (m == null || m.isEmpty()) continue;
            ArrayList<String> keys = new ArrayList<>(m.keySet());
            Collections.sort(keys);
            for (String k : keys) {
                if (c.methods.contains(k)) {
                    sb.append("  ").append(c.name.replace('/', '.')).append('.').append(k).append("  ->  ").append(m.get(k)).append('\n');
                    if (++shown >= 15) break;
                }
            }
            if (shown >= 15) break;
        }
        if (conflicts > 0) {
            sb.append("\nconflict samples\n");
            for (String s : conflictSamples) sb.append("  ").append(s).append('\n');
        }
        if (!missingSamples.isEmpty()) {
            sb.append("\nunmatched tsrg members (sample)\n");
            for (String s : missingSamples) sb.append("  ").append(s).append('\n');
        }
        if (checkJar != null && Files.exists(checkJar)) {
            ArrayList<String> bad = new ArrayList<>(), decl = new ArrayList<>();
            int badCount = checkJar(checkJar, info, bad, decl);
            sb.append("\nCHECK of ").append(checkJar).append('\n');
            sb.append("  members still official but mapped to something else: ").append(badCount).append('\n');
            for (String s : bad) sb.append("    ref  ").append(s).append('\n');
            for (String s : decl) sb.append("    decl ").append(s).append('\n');
        }
        if (report != null) {
            Files.createDirectories(report.toAbsolutePath().getParent());
            Files.write(report, sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        System.out.print(sb);
    }

    static String pad(String s) {
        StringBuilder b = new StringBuilder(s);
        while (b.length() < 26) b.append(' ');
        return b.toString();
    }

    static byte[] readAll(ZipFile z, ZipEntry e) throws IOException {
        try (InputStream in = z.getInputStream(e)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.max(64, (int) Math.max(0, e.getSize())));
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }
}
