import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GenerateCountrysideMap {
    static final int W = 180;
    static final int H = 64;
    static final int TW = 64;
    static final int TH = 32;
    static final int OBJ_W = 64;
    static final int OBJ_H = 96;
    static final int OBJ_COLS = 8;
    static final int GID_GRASS = 1;
    static final int GID_DIRT = 57;
    static final int GID_STONE = 113;
    static final int GID_FOREST = 169;
    static final int GID_OBJECTS = 225;
    static final long TERRAIN_SEED = 20260517L;

    record Obj(String name, String source, int cropX, int cropY, int cropW, int cropH) {}
    record Placement(int x, int y, int gid, String type) {}

    static final List<Obj> OBJECTS = List.of(
        new Obj("tree_oak", "assets/map/isometricTrees_green.png", 0, 0, 512, 512),
        new Obj("tree_pine", "assets/map/isometricTrees_green.png", 512, 0, 512, 512),
        new Obj("tree_round", "assets/map/isometricTrees_green.png", 1024, 0, 512, 512),
        new Obj("tree_small", "assets/map/isometricTrees_green.png", 1536, 0, 512, 512),
        new Obj("fence_high_e", "assets/map/kenney_isometric-miniature-farm/Isometric/fenceHigh_E.png", 0, 0, 256, 512),
        new Obj("fence_high_s", "assets/map/kenney_isometric-miniature-farm/Isometric/fenceHigh_S.png", 0, 0, 256, 512),
        new Obj("fence_broken_e", "assets/map/kenney_isometric-miniature-farm/Isometric/fenceHighBroken_E.png", 0, 0, 256, 512),
        new Obj("fence_broken_s", "assets/map/kenney_isometric-miniature-farm/Isometric/fenceHighBroken_S.png", 0, 0, 256, 512),
        new Obj("hay_bales", "assets/map/kenney_isometric-miniature-farm/Isometric/hayBales_E.png", 0, 0, 256, 512),
        new Obj("hay_stack", "assets/map/kenney_isometric-miniature-farm/Isometric/hayBalesStacked_E.png", 0, 0, 256, 512),
        new Obj("sacks_crate", "assets/map/kenney_isometric-miniature-farm/Isometric/sacksCrate_E.png", 0, 0, 256, 512),
        new Obj("sack", "assets/map/kenney_isometric-miniature-farm/Isometric/sack_E.png", 0, 0, 256, 512),
        new Obj("corn", "assets/map/kenney_isometric-miniature-farm/Isometric/corn_E.png", 0, 0, 256, 512),
        new Obj("corn_double", "assets/map/kenney_isometric-miniature-farm/Isometric/cornDouble_E.png", 0, 0, 256, 512),
        new Obj("corn_young", "assets/map/kenney_isometric-miniature-farm/Isometric/cornYoung_E.png", 0, 0, 256, 512),
        new Obj("ladder_stand", "assets/map/kenney_isometric-miniature-farm/Isometric/ladderStand_E.png", 0, 0, 256, 512),
        new Obj("planks", "assets/map/kenney_isometric-miniature-farm/Isometric/planks_E.png", 0, 0, 256, 512),
        new Obj("planks_old", "assets/map/kenney_isometric-miniature-farm/Isometric/planksOld_E.png", 0, 0, 256, 512),
        new Obj("wall", "assets/map/kenney_isometric-miniature-farm/Isometric/woodWall_E.png", 0, 0, 256, 512),
        new Obj("wall_window", "assets/map/kenney_isometric-miniature-farm/Isometric/woodWallWindow_E.png", 0, 0, 256, 512),
        new Obj("wall_door", "assets/map/kenney_isometric-miniature-farm/Isometric/woodWallDoorClosed_E.png", 0, 0, 256, 512),
        new Obj("roof", "assets/map/kenney_isometric-miniature-farm/Isometric/roof_E.png", 0, 0, 256, 512),
        new Obj("roof_corner", "assets/map/kenney_isometric-miniature-farm/Isometric/roofCorner_E.png", 0, 0, 256, 512),
        new Obj("chimney", "assets/map/kenney_isometric-miniature-farm/Isometric/chimneyTop_E.png", 0, 0, 256, 512)
    );

    public static void main(String[] args) throws Exception {
        Path root = Path.of("").toAbsolutePath();
        Path outDir = root.resolve("assets/map/generated");
        Files.createDirectories(outDir);

        writeObjectAtlas(root, outDir.resolve("countryside_objects.png"));

        int[][] ground = new int[H][W];
        int[][] details = new int[H][W];
        int[][] decor = new int[H][W];
        int[][] collision = new int[H][W];
        List<Placement> placements = new ArrayList<>();
        buildMap(ground, details, decor, collision, placements);

        Files.writeString(outDir.resolve("countryside_long.tmx"), tmx(ground, details, decor, collision, placements), StandardCharsets.UTF_8);
        renderPreview(root, outDir.resolve("countryside_long.png"), ground, details, decor);
    }

    static int obj(String name) {
        for (int i = 0; i < OBJECTS.size(); i++) if (OBJECTS.get(i).name.equals(name)) return GID_OBJECTS + i;
        throw new IllegalArgumentException(name);
    }

    static void buildMap(int[][] ground, int[][] details, int[][] decor, int[][] collision, List<Placement> placements) {
        Random terrainRandom = new Random(TERRAIN_SEED);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int roll = terrainRandom.nextInt(100);
                ground[y][x] = GID_GRASS + (roll < 8 ? 2 : roll < 16 ? 9 : roll < 22 ? 17 : 0);
            }
        }
        addGroundPatches(ground, details, terrainRandom);

        for (int x = 0; x < W; x++) {
            int cy = roadY(x);
            for (int dy = -1; dy <= 1; dy++) {
                int y = cy + dy;
                if (in(x, y)) {
                    ground[y][x] = GID_DIRT + (Math.abs(x + dy) % 5);
                    details[y][x] = (x % 9 == 0 && dy == 0) ? GID_STONE + 3 : 0;
                }
            }
            if (x % 13 == 0 && in(x, cy - 2)) details[cy - 2][x] = GID_STONE + 11;
            if (x % 17 == 4 && in(x, cy + 2)) details[cy + 2][x] = GID_STONE + 13;
        }

        addField(ground, decor, collision, placements, 8, 5, 18, 8);
        addField(ground, decor, collision, placements, 18, 42, 20, 9);
        addField(ground, decor, collision, placements, 42, 18, 22, 9);
        addField(ground, decor, collision, placements, 68, 4, 24, 9);
        addField(ground, decor, collision, placements, 83, 44, 24, 10);
        addField(ground, decor, collision, placements, 111, 20, 22, 9);
        addField(ground, decor, collision, placements, 135, 6, 25, 10);
        addField(ground, decor, collision, placements, 148, 42, 24, 10);

        addFenceLine(decor, collision, placements, 5, 4, 25, false);
        addFenceLine(decor, collision, placements, 16, 52, 26, false);
        addFenceLine(decor, collision, placements, 39, 17, 29, false);
        addFenceLine(decor, collision, placements, 66, 3, 31, false);
        addFenceLine(decor, collision, placements, 82, 55, 32, false);
        addFenceLine(decor, collision, placements, 108, 19, 30, false);
        addFenceLine(decor, collision, placements, 132, 5, 32, false);
        addFenceLine(decor, collision, placements, 145, 54, 33, false);
        addFenceLine(decor, collision, placements, 28, 24, 18, true);
        addFenceLine(decor, collision, placements, 123, 31, 20, true);

        int[][] obstacleGroups = {
            {18, roadY(18), obj("hay_stack")}, {27, roadY(27) - 5, obj("planks_old")},
            {43, roadY(43) + 5, obj("sacks_crate")}, {54, roadY(54), obj("fence_broken_e")},
            {76, roadY(76) - 6, obj("hay_bales")}, {84, roadY(84) + 6, obj("ladder_stand")},
            {104, roadY(104), obj("planks")}, {117, roadY(117) - 5, obj("sack")},
            {137, roadY(137) + 6, obj("fence_broken_s")}, {150, roadY(150), obj("hay_stack")},
            {164, roadY(164) - 4, obj("sacks_crate")}
        };
        for (int[] o : obstacleGroups) place(decor, collision, placements, o[0], o[1], o[2], objectName(o[2]), true);

        for (int x = 2; x < W; x += 7) {
            int topY = 1 + Math.floorMod(x * 5, 4);
            int botY = H - 3 - Math.floorMod(x * 7, 5);
            place(decor, collision, placements, x, topY, obj("tree_oak"), "tree_oak", true);
            if (x + 3 < W) {
                String name = (x % 3 == 0) ? "tree_pine" : "tree_round";
                place(decor, collision, placements, x + 3, botY, obj(name), name, true);
            }
        }

        addFarmhouse(decor, collision, placements, 55, 35);
        addFarmhouse(decor, collision, placements, 119, 12);
        addFarmhouse(decor, collision, placements, 153, 31);
    }

    static int roadY(int x) {
        return H / 2 + Math.round((float)(Math.sin(x / 10.0) * 8.0 + Math.sin(x / 23.0) * 5.0));
    }

    static void addGroundPatches(int[][] ground, int[][] details, Random random) {
        for (int i = 0; i < 90; i++) {
            int sx = random.nextInt(W);
            int sy = random.nextInt(H);
            int patchW = 2 + random.nextInt(4);
            int patchH = 1 + random.nextInt(3);
            int tile = random.nextBoolean() ? GID_GRASS + 24 : GID_FOREST + 6;
            for (int y = sy; y < sy + patchH; y++) {
                for (int x = sx; x < sx + patchW; x++) {
                    if (!in(x, y) || random.nextInt(100) < 25) continue;
                    if (tile >= GID_FOREST) details[y][x] = tile;
                    else ground[y][x] = tile;
                }
            }
        }
    }

    static void addField(int[][] ground, int[][] decor, int[][] collision, List<Placement> placements, int sx, int sy, int w, int h) {
        for (int y = sy; y < sy + h; y++) {
            for (int x = sx; x < sx + w; x++) {
                if (!in(x, y)) continue;
                ground[y][x] = GID_DIRT + 8 + Math.floorMod(x + y, 4);
                if ((x + y) % 2 == 0) {
                    String crop = (x + y) % 4 == 0 ? "corn_double" : "corn";
                    place(decor, collision, placements, x, y, obj(crop), crop, false);
                }
                else if ((x + y) % 5 == 0) decor[y][x] = obj("corn_young");
            }
        }
    }

    static void addFenceLine(int[][] decor, int[][] collision, List<Placement> placements, int sx, int y, int len, boolean vertical) {
        for (int i = 0; i < len; i++) {
            if (i % 11 == 5) continue;
            int x = vertical ? sx : sx + i;
            int yy = vertical ? y + i : y;
            String name = i % 6 == 3 ? "fence_broken_e" : vertical ? "fence_high_s" : "fence_high_e";
            place(decor, collision, placements, x, yy, obj(name), name, true);
        }
    }

    static void addFarmhouse(int[][] decor, int[][] collision, List<Placement> placements, int sx, int sy) {
        int[][] house = {
            {obj("roof_corner"), obj("roof"), obj("roof")},
            {obj("wall_window"), obj("wall"), obj("wall_door")},
            {obj("hay_bales"), obj("sacks_crate"), obj("chimney")}
        };
        for (int y = 0; y < house.length; y++) {
            for (int x = 0; x < house[y].length; x++) {
                place(decor, collision, placements, sx + x, sy + y, house[y][x], objectName(house[y][x]), true);
            }
        }
    }

    static void place(int[][] decor, int[][] collision, List<Placement> placements, int x, int y, int gid, String type, boolean rigid) {
        if (!in(x, y)) return;
        decor[y][x] = gid;
        if (rigid) {
            collision[y][x] = gid;
            placements.add(new Placement(x, y, gid, type));
        }
    }

    static boolean in(int x, int y) { return x >= 0 && y >= 0 && x < W && y < H; }

    static String objectName(int gid) {
        int index = gid - GID_OBJECTS;
        if (index < 0 || index >= OBJECTS.size()) return "unknown";
        return OBJECTS.get(index).name;
    }

    static void writeObjectAtlas(Path root, Path out) throws IOException {
        int rows = (int)Math.ceil(OBJECTS.size() / (double)OBJ_COLS);
        BufferedImage atlas = new BufferedImage(OBJ_COLS * OBJ_W, rows * OBJ_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        quality(g);
        g.setComposite(AlphaComposite.Src);
        for (int i = 0; i < OBJECTS.size(); i++) {
            Obj o = OBJECTS.get(i);
            BufferedImage src = ImageIO.read(root.resolve(o.source).toFile()).getSubimage(o.cropX, o.cropY, o.cropW, o.cropH);
            int[] box = trim(src);
            BufferedImage trimmed = src.getSubimage(box[0], box[1], box[2], box[3]);
            double scale = Math.min((OBJ_W - 6) / (double)trimmed.getWidth(), (OBJ_H - 6) / (double)trimmed.getHeight());
            int dw = Math.max(1, (int)Math.round(trimmed.getWidth() * scale));
            int dh = Math.max(1, (int)Math.round(trimmed.getHeight() * scale));
            int dx = (i % OBJ_COLS) * OBJ_W + (OBJ_W - dw) / 2;
            int dy = (i / OBJ_COLS) * OBJ_H + OBJ_H - dh - 2;
            g.drawImage(trimmed, dx, dy, dw, dh, null);
        }
        g.dispose();
        ImageIO.write(atlas, "png", out.toFile());
    }

    static int[] trim(BufferedImage img) {
        int minX = img.getWidth(), minY = img.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < img.getHeight(); y++) for (int x = 0; x < img.getWidth(); x++) {
            if (((img.getRGB(x, y) >>> 24) & 0xff) > 8) {
                minX = Math.min(minX, x); minY = Math.min(minY, y); maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX) return new int[]{0, 0, img.getWidth(), img.getHeight()};
        return new int[]{minX, minY, maxX - minX + 1, maxY - minY + 1};
    }

    static String tmx(int[][] ground, int[][] details, int[][] decor, int[][] collision, List<Placement> placements) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<map version=\"1.10\" tiledversion=\"1.11.0\" orientation=\"isometric\" renderorder=\"right-down\" width=\"").append(W).append("\" height=\"").append(H).append("\" tilewidth=\"").append(TW).append("\" tileheight=\"").append(TH).append("\" infinite=\"0\" nextlayerid=\"6\" nextobjectid=\"").append(placements.size() + 5).append("\">\n");
        tileset(sb, GID_GRASS, "grass_green", "../ground_tiles_sheets/grass_green_64x32.png", 64, 32, 56, 8);
        tileset(sb, GID_DIRT, "dirt", "../ground_tiles_sheets/dirt_64x32.png", 64, 32, 56, 8);
        tileset(sb, GID_STONE, "stone_path", "../ground_tiles_sheets/stone_path_64x32.png", 64, 32, 56, 8);
        tileset(sb, GID_FOREST, "forest_ground", "../ground_tiles_sheets/forest_ground_64x32.png", 64, 32, 56, 8);
        sb.append(" <tileset firstgid=\"").append(GID_OBJECTS).append("\" name=\"countryside_objects\" tilewidth=\"").append(OBJ_W).append("\" tileheight=\"").append(OBJ_H).append("\" tilecount=\"").append(OBJECTS.size()).append("\" columns=\"").append(OBJ_COLS).append("\">\n");
        sb.append("  <tileoffset x=\"0\" y=\"-64\"/>\n");
        for (int i = 0; i < OBJECTS.size(); i++) {
            sb.append("  <tile id=\"").append(i).append("\" type=\"obstacle\"><properties><property name=\"name\" value=\"").append(OBJECTS.get(i).name).append("\"/><property name=\"collidable\" type=\"bool\" value=\"true\"/></properties></tile>\n");
        }
        sb.append("  <image source=\"countryside_objects.png\" width=\"").append(OBJ_COLS * OBJ_W).append("\" height=\"").append(((OBJECTS.size() + OBJ_COLS - 1) / OBJ_COLS) * OBJ_H).append("\"/>\n");
        sb.append(" </tileset>\n");
        layer(sb, 1, "Ground", ground, true);
        layer(sb, 2, "Path_Details", details, true);
        layer(sb, 3, "Obstacles_And_Props", decor, true);
        layer(sb, 4, "Collision", collision, false);
        rigidBodies(sb, placements);
        sb.append("</map>\n");
        return sb.toString();
    }

    static void rigidBodies(StringBuilder sb, List<Placement> placements) {
        int objectId = 1;
        sb.append(" <objectgroup id=\"5\" name=\"RigidBodies\" visible=\"0\">\n");
        for (Placement placement : placements) {
            float[] world = tileToWorld(placement.x, placement.y);
            float width = rigidWidth(placement.type);
            float height = rigidHeight(placement.type);
            float x = world[0] + TW / 2f - width / 2f;
            float y = world[1] + TH / 2f - height / 2f;
            rigidObject(sb, objectId++, placement.type, "solid", x, y, width, height);
        }
        float left = 0f;
        float right = (W + H) * TW / 2f;
        float bottom = -W * TH / 2f;
        float top = H * TH / 2f + OBJ_H;
        rigidObject(sb, objectId++, "north_bound", "boundary", left, top, right - left, 32f);
        rigidObject(sb, objectId++, "south_bound", "boundary", left, bottom - 32f, right - left, 32f);
        rigidObject(sb, objectId++, "west_bound", "boundary", left - 32f, bottom, 32f, top - bottom);
        rigidObject(sb, objectId, "east_bound", "boundary", right, bottom, 32f, top - bottom);
        sb.append(" </objectgroup>\n");
    }

    static void rigidObject(StringBuilder sb, int id, String name, String type, float x, float y, float width, float height) {
        sb.append("  <object id=\"").append(id).append("\" name=\"").append(name).append("\" type=\"").append(type)
                .append("\" x=\"").append(format(x)).append("\" y=\"").append(format(y)).append("\" width=\"")
                .append(format(width)).append("\" height=\"").append(format(height)).append("\">\n");
        sb.append("   <properties>\n");
        sb.append("    <property name=\"worldX\" type=\"float\" value=\"").append(format(x)).append("\"/>\n");
        sb.append("    <property name=\"worldY\" type=\"float\" value=\"").append(format(y)).append("\"/>\n");
        sb.append("    <property name=\"worldWidth\" type=\"float\" value=\"").append(format(width)).append("\"/>\n");
        sb.append("    <property name=\"worldHeight\" type=\"float\" value=\"").append(format(height)).append("\"/>\n");
        sb.append("   </properties>\n");
        sb.append("  </object>\n");
    }

    static float[] tileToWorld(int x, int y) {
        return new float[] {(x + y) * TW / 2f, (y - x) * TH / 2f};
    }

    static float rigidWidth(String type) {
        if (type.startsWith("fence")) return 58f;
        if (type.startsWith("tree")) return 34f;
        if (type.startsWith("roof") || type.startsWith("wall")) return 54f;
        return 42f;
    }

    static float rigidHeight(String type) {
        if (type.startsWith("fence")) return 20f;
        if (type.startsWith("tree")) return 32f;
        if (type.startsWith("roof") || type.startsWith("wall")) return 44f;
        return 34f;
    }

    static String format(float value) {
        if (Math.abs(value - Math.round(value)) < 0.001f) return Integer.toString(Math.round(value));
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    static void tileset(StringBuilder sb, int firstgid, String name, String source, int tw, int th, int count, int cols) {
        sb.append(" <tileset firstgid=\"").append(firstgid).append("\" name=\"").append(name).append("\" tilewidth=\"").append(tw).append("\" tileheight=\"").append(th).append("\" tilecount=\"").append(count).append("\" columns=\"").append(cols).append("\">\n");
        sb.append("  <image source=\"").append(source).append("\" width=\"512\" height=\"224\"/>\n");
        sb.append(" </tileset>\n");
    }

    static void layer(StringBuilder sb, int id, String name, int[][] data, boolean visible) {
        sb.append(" <layer id=\"").append(id).append("\" name=\"").append(name).append("\" width=\"").append(W).append("\" height=\"").append(H).append("\" visible=\"").append(visible ? 1 : 0).append("\">\n");
        sb.append("  <data encoding=\"csv\">\n");
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                sb.append(data[y][x]);
                if (!(x == W - 1 && y == H - 1)) sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("  </data>\n </layer>\n");
    }

    static void renderPreview(Path root, Path out, int[][] ground, int[][] details, int[][] decor) throws IOException {
        Map<Integer, BufferedImage> cache = new HashMap<>();
        int ox = H * TW / 2 + 80;
        int pw = (W + H) * TW / 2 + 160;
        int ph = (W + H) * TH / 2 + OBJ_H + 120;
        BufferedImage img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        quality(g);
        g.setColor(new java.awt.Color(105, 153, 84));
        g.fillRect(0, 0, pw, ph);
        drawLayer(g, root, cache, ground, ox, 30);
        drawLayer(g, root, cache, details, ox, 30);
        drawLayer(g, root, cache, decor, ox, 30);
        g.dispose();
        ImageIO.write(img, "png", out.toFile());
    }

    static void drawLayer(Graphics2D g, Path root, Map<Integer, BufferedImage> cache, int[][] layer, int ox, int oy) throws IOException {
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            int gid = layer[y][x];
            if (gid == 0) continue;
            BufferedImage tile = cache.computeIfAbsent(gid, id -> {
                try { return tileImage(root, id); } catch (IOException e) { throw new RuntimeException(e); }
            });
            int sx = ox + (x - y) * TW / 2;
            int sy = oy + (x + y) * TH / 2;
            if (gid >= GID_OBJECTS) g.drawImage(tile, sx, sy - OBJ_H + TH, null);
            else g.drawImage(tile, sx, sy, null);
        }
    }

    static BufferedImage tileImage(Path root, int gid) throws IOException {
        if (gid >= GID_OBJECTS) {
            BufferedImage atlas = ImageIO.read(root.resolve("assets/map/generated/countryside_objects.png").toFile());
            int id = gid - GID_OBJECTS;
            return atlas.getSubimage((id % OBJ_COLS) * OBJ_W, (id / OBJ_COLS) * OBJ_H, OBJ_W, OBJ_H);
        }
        String path;
        int local;
        if (gid >= GID_FOREST) { path = "assets/map/ground_tiles_sheets/forest_ground_64x32.png"; local = gid - GID_FOREST; }
        else if (gid >= GID_STONE) { path = "assets/map/ground_tiles_sheets/stone_path_64x32.png"; local = gid - GID_STONE; }
        else if (gid >= GID_DIRT) { path = "assets/map/ground_tiles_sheets/dirt_64x32.png"; local = gid - GID_DIRT; }
        else { path = "assets/map/ground_tiles_sheets/grass_green_64x32.png"; local = gid - GID_GRASS; }
        BufferedImage sheet = ImageIO.read(root.resolve(path).toFile());
        return sheet.getSubimage((local % 8) * 64, (local / 8) * 32, 64, 32);
    }

    static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
