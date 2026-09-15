# Cómo subir este proyecto a GitHub

1. Crea un repositorio vacío, por ejemplo: `mundo-matematico-samuel`.
2. Descomprime el ZIP.
3. Sube **todos los archivos que están dentro**, no la carpeta contenedora.
4. Comprueba que en la raíz de GitHub se vean:
   - `MainActivity.java`
   - `AndroidManifest.xml`
   - `build-apk.sh`
   - `.github/workflows/build-apk.yml`
5. Haz commit en la rama `main`.
6. Ve a **Actions** → `Compilar APK - Samuel`.
7. Pulsa **Run workflow**.
8. Cuando termine en verde, descarga el artefacto `Mundo_Matematico_Samuel_APK`.
