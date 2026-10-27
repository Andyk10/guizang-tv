"""Wallpaper pack checks: originals, dimensions, stale assets and failed imports."""
import ast
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import unittest

from PIL import Image


class WallpaperPackTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / "scripts").mkdir()
        self.script = self.root / "scripts/prepare_wallpapers.py"
        shutil.copy2(Path(__file__).with_name("prepare_wallpapers.py"), self.script)
        self.output = self.root / "app/src/main/assets/wallpapers/local"

    def images(self, count, name):
        folder = self.root / name
        folder.mkdir()
        for i in range(count):
            Image.new("RGB", (2000, 1200), (i, 30, 50)).save(folder / f"{i:02d}.jpg")
        return folder

    def run_script(self, folder, env=None):
        return subprocess.run([sys.executable, str(self.script), str(folder)],
                              capture_output=True, env=env)

    def test_supported_sizes_and_originals(self):
        for count in (1, 3, 13, 16, 20, 30):
            with self.subTest(count=count):
                folder = self.images(count, f"images-{count}")
                before = {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in folder.iterdir()}
                result = self.run_script(folder)
                self.assertEqual(result.returncode, 0, result.stderr)
                catalog = json.loads((self.output / "catalog.json").read_text(encoding="utf-8"))
                self.assertEqual(len(catalog), count)
                self.assertEqual(len(list(self.output.glob("*.jpg"))), count * 2)
                for entry in catalog:
                    with Image.open(self.output / Path(entry["file"]).name) as image:
                        self.assertLessEqual(max(image.size), 1920)
                    with Image.open(self.output / Path(entry["thumb"]).name) as image:
                        self.assertEqual(image.size, (320, 180))
                self.assertEqual(before, {p.name: hashlib.sha256(p.read_bytes()).hexdigest()
                                          for p in folder.iterdir()})

    def test_python_and_android_limits_match(self):
        source = Path(__file__).with_name("prepare_wallpapers.py").read_text(encoding="utf-8")
        limits = [ast.literal_eval(node.value) for node in ast.parse(source).body
                  if isinstance(node, ast.Assign)
                  and any(isinstance(target, ast.Name) and target.id == "MAX_WALLPAPERS"
                          for target in node.targets)]
        self.assertEqual(limits, [30])
        java = (Path(__file__).resolve().parents[1] /
                "app/src/main/java/io/github/op7418/guizangtv/FrameWallpapers.java")
        limit = re.search(r"MAX_WALLPAPERS\s*=\s*(\d+)\s*;", java.read_text(encoding="utf-8"))
        self.assertIsNotNone(limit)
        self.assertEqual(int(limit.group(1)), limits[0])

    def test_catalog_preserves_unicode_names_as_utf8(self):
        folder = self.root / "unicode-images"
        folder.mkdir()
        names = ["01_\u5361\u7279\u7433\u5a1c\u5c9b", "02_\u592a\u6d69\u6e56_\U0001f304"]
        for name in names:
            Image.new("RGB", (640, 360), (20, 30, 50)).save(folder / (name + ".png"))
        # Exercise the legacy locale encoding on Windows, even under a UTF-8 parent.
        env = dict(os.environ, PYTHONUTF8="0", PYTHONCOERCECLOCALE="0", PYTHONIOENCODING="utf-8")
        result = self.run_script(folder, env=env)
        self.assertEqual(result.returncode, 0, result.stderr)
        catalog = json.loads((self.output / "catalog.json").read_text(encoding="utf-8"))
        self.assertEqual([entry["name"] for entry in catalog], names)

    def test_replacement_keeps_backup_but_does_not_bundle_old_images(self):
        self.assertEqual(self.run_script(self.images(3, "old")).returncode, 0)
        self.assertEqual(self.run_script(self.images(1, "new")).returncode, 0)
        self.assertEqual(len(list(self.output.glob("*.jpg"))), 2)
        backups = list((self.root / ".wallpaper-backups").glob("previous-*"))
        self.assertEqual(len(backups), 1)
        self.assertEqual(len(list(backups[0].glob("*.jpg"))), 6)

    def test_failed_conversion_preserves_current_pack(self):
        self.assertEqual(self.run_script(self.images(1, "valid")).returncode, 0)
        before = {p.name: p.read_bytes() for p in self.output.iterdir()}
        broken = self.root / "broken"
        broken.mkdir()
        (broken / "broken.jpg").write_bytes(b"not an image")
        self.assertNotEqual(self.run_script(broken).returncode, 0)
        self.assertEqual(before, {p.name: p.read_bytes() for p in self.output.iterdir()})

    def test_rejects_empty_and_oversized_catalog(self):
        for count in (0, 31):
            with self.subTest(count=count):
                result = self.run_script(self.images(count, f"invalid-{count}"))
                self.assertEqual(result.returncode, 2, result.stderr)
        self.assertFalse(self.output.exists())

    def test_invalid_count_preserves_current_pack(self):
        self.assertEqual(self.run_script(self.images(1, "valid")).returncode, 0)
        before = {p.name: p.read_bytes() for p in self.output.iterdir()}
        for count in (0, 31):
            with self.subTest(count=count):
                result = self.run_script(self.images(count, f"invalid-{count}"))
                self.assertEqual(result.returncode, 2, result.stderr)
                self.assertEqual(before, {p.name: p.read_bytes() for p in self.output.iterdir()})


if __name__ == "__main__":
    unittest.main()
