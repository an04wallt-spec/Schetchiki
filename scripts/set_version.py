from pathlib import Path
import re, sys

if len(sys.argv) != 4:
    raise SystemExit('usage: set_version.py <work_dir> <version_code> <version_name>')

work = Path(sys.argv[1])
version_code = str(int(sys.argv[2]))
version_name = sys.argv[3].strip()
path = work / 'apktool.yml'
text = path.read_text(encoding='utf-8')
text = re.sub(r'(?m)^\s*versionCode:\s*.*$', f'  versionCode: {version_code}', text)
text = re.sub(r'(?m)^\s*versionName:\s*.*$', f'  versionName: {version_name}', text)
path.write_text(text, encoding='utf-8')
print(f'VERSION_SET code={version_code} name={version_name}')
