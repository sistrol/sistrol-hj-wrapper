![Sistrol - Edison Next](https://www.sistrol.com/wp-content/uploads/2021/04/LOGO-SISTROL-Y-EDISON-NEXT_negativo-e1652274617530.png)

<h1>sistrol-haystack-java sample code</h1>

<!-- TOC -->
- [1. Motivación](#1-motivación)
- [2. Intro](#2-intro)
- [3. Requerimientos previos](#3-requerimientos-previos)
- [4. Cómo funciona el módulo NHaystack en Niagara](#4-cómo-funciona-el-módulo-nhaystack-en-niagara)
- [5. El API REST Haystack](#5-el-api-rest-haystack)
- [6. Operación **read**](#6-operación-read)
  - [6.1. Cuerpo de la petición](#61-cuerpo-de-la-petición)
  - [6.2. Respuesta OK desde station Niagara](#62-respuesta-ok-desde-station-niagara)
- [7. Operación **hisRead**](#7-operación-hisread)
  - [7.1. Cuerpo de la petición](#71-cuerpo-de-la-petición)
    - [7.1.1. Obtener id de punto desde respuesta de **read**](#711-obtener-id-de-punto-desde-respuesta-de-read)
    - [7.1.2. Qué valores se pueden indicar en _range_](#712-qué-valores-se-pueden-indicar-en-range)
  - [7.2. Respuesta OK desde station Niagara](#72-respuesta-ok-desde-station-niagara)
- [8. Gestión de errores](#8-gestión-de-errores)
  - [8.1. Errores HTTP](#81-errores-http)
  - [8.2. Errores Haystack](#82-errores-haystack)
- [9. Recursos](#9-recursos)
<!-- TOC -->


Este repo contiene un ejemplo de la utilización combinada de la librería **haystack-java** junto con los componentes HTTP nativos de Java

# 1. Motivación
Los motivos para trabajar de esta forma son:
* de rendimiento: la librería _haystack-java_ utiliza internamente componentes HTTP nativos de versiones antiguas de Java, en algunos casos ya obsoletos. El rendimiento que dan estas librerias es pobre en algunos tipos de peticiones.
* de formato: la librería _haystack-java_ utiliza formatos _propietarios_ que no son compatibles con sistemas/librerías de terceros, y cuya conversión a formatos estándar (p.e. JSON) no es sencilla.

**¿Por qué no prescindir de _haystack-java_ y hacerlo todo con librerías HTTP _regulares_?**
Por la autenticación. La librería _hasystack-java_ implementa los métodos de autenticación utilizados por los sistemas _compatibles_ con NHaystack, contemplando en esa implementación algunas peculiaridades.

# 2. Intro
En este repo se proporcionan ejemplos para dos operaciones:
* *read* nos permitirá enviar consultas a Niagara para obtener listados de históricos y puntos.
* *hisRead* nos permite leer los datos de históricos configurados en Niagara (un histórico en cada petición)

# 3. Requerimientos previos
Hay que instalar los módulos **nhaystack-rt** y **nhaystack-wb** en la station de Niagara a la que vamos a enviar las consultas, copiándolos a la carpeta _${NIAGARA_HOME}/modules. Tras hacer esto, es necesario reiniciar la station.
Si ejecutamos nuestro workbench en un host distinto al que corre la station de Niagara, debemos instalar los módulos también en el host del workbench para que los componentes NHaystack estén disponibles en la paleta de componentes. En cualquier caso, también debemos reiniciar el workbench tras la instalación de los módulos.
Los módulos se pueden descargar desde [https://stackhub.org/package/nHaystack](https://stackhub.org/package/nHaystack)

Una vez instalados los módulos:
* abrir la paleta _NHaystack_ en el workbench y arrastrar el componente **Haystack Service** a la carpeta _/Services_ de la station.
* abrir la hoja de propiedades del componente _Haystack Service_ y habilitar el servlet integrado que implementa el API REST Haystack.

Hay información más detallada en el [repositorio del módulo NHaystack en GitHub](https://github.com/ci-richard-mcelhinney/nhaystack)

# 4. Cómo funciona el módulo NHaystack en Niagara
El funcionamiento del módulo **NHaystack** es complejo y queda fuera del alcance de este ejemplo.

Al instalar en Niagara el servicio _NHaystack Service_, los componentes de tipo **Control Point** y **History** quedan automáticamente etiquetados con `point` y otras etiquetas relacionadas como `kind` (indica el tipo de datos que podrá ser _Number_, _Bool_ o _Str_), `unit` (que indica las unidades en el caso de los tipos numéricos), `tz` (con el _timezone_ del punto), `his` (un marcador que distingue a los históricos) o `cur` (para aquellos componentes de tipo _Control Point_ que tienen en tiempo real el valor del punto en el terreno).

El módulo permite asignar diferentes etiquetas a los distintos componentes de la station, creando una o varias jerarquías de componentes, árboles de navegación, etc. Pero eso queda fuera de las funciones que se pretenden mostrar con este ejemplo.

# 5. El API REST Haystack
* El API REST Haystack define un conjunto de operaciones cuya documentación se puede consultar en este [enlace](https://project-haystack.org/doc/docHaystack/Ops). En este ejemplo únicamente se cubren las operaciones (Read)[https://project-haystack.org/doc/docHaystack/Ops#read] y (HisRead)[https://project-haystack.org/doc/docHaystack/Ops#hisRead] 

# 6. Operación **read**
## 6.1. Cuerpo de la petición
El cuerpo de la petición debe ser un _grid en formato ZINC_. Esto no es más que un CSV precedido de una línea `ver: "3.0"` antes de la línea de cabeceras. Ejemplo para la operación **read**
```
ver: "3.0"
filter,limit
"his and kind==\"Number\" and not cur",N
```

La petición tendrá una línea de cabeceras `ts,limit` y una única _línea de datos_ con dos valores:
* _filter_: se trata de una expresión que se debe construir de acuerdo a las reglas que se indican en este [enlace](https://project-haystack.org/doc/docHaystack/Filters)
* _limit_: número entero mayor que 0 que indica el número máximo de resultados que se obtendrán. Se debe utilizar `N` (equivalente a _null_) si se quieren obtener todos los resultados.

La operación **read** también se puede invocar pasando una lista de identificadores, en cuyo caso el cuerpo de la petición tendría una estructura como esta:
```
ver: "3.0"
id
@vav101.zoneTemp
@vav102.zoneTemp
@vav103.zoneTemp
```
En este caso, puede haber más de una línea de datos en la petición y la respuesta contendrá el mismo número de filas. Si no se encuentra el punto correspondiente a un identificador de la lista, en su lugar aparecerá una línea con todos los valores nulos (`N`)

## 6.2. Respuesta OK desde station Niagara
La respuesta en caso de éxito tendrá la siguiente estructura. Se asume que en la petición se indicó que se esperaba la respuesta en formato JSON
```json
{
  "meta": {
    "ver": "3.0"
  },
  "rows": [
    {
      "unit": "kWh",
      "his": "m:",
      "tz": "Paris",
      "kind": "Number",
      "axHistoryId": "\/Alvia_J1\/AR_GC03_UPS_EnerActTot",
      "axType": "s:history:HistoryConfig",
      "navName": "Alvia_J1_AR_GC03_UPS_EnerActTot",
      "id": "r:H.Alvia_J1.AR_GC03_UPS_EnerActTot Alvia_J1_AR_GC03_UPS_EnerActTot",
      "point": "m:",
      "dis": "Alvia_J1_AR_GC03_UPS_EnerActTot"
    },
    {
      "unit": "kWh",
      "his": "m:",
      "tz": "Paris",
      "kind": "Number",
      "axHistoryId": "\/Alvia_J1\/AR_P1_Ofi_11_EnerActTot",
      "axType": "s:history:HistoryConfig",
      "navName": "Alvia_J1_AR_P1_Ofi_11_EnerActTot",
      "id": "r:H.Alvia_J1.AR_P1_Ofi_11_EnerActTot Alvia_J1_AR_P1_Ofi_11_EnerActTot",
      "point": "m:",
      "dis": "Alvia_J1_AR_P1_Ofi_11_EnerActTot"
    },
    ...
  ],
  "cols": [
    {
      "name": "tz"
    },
    {
      "name": "axHistoryId"
    },
    {
      "name": "point"
    },
    {
      "name": "dis"
    },
    {
      "name": "his"
    },
    {
      "name": "axType"
    },
    {
      "name": "navName"
    },
    {
      "name": "id"
    },
    {
      "name": "kind"
    },
    {
      "name": "unit"
    }
  ]
}
```
* _meta_ no contiene info significativa si la petición se completa con éxito
* _rows_ es un array de docs con la info de cada punto recuperado. En cada doc:
  * _unit_: cadena con las unidades configuradas para el punto. Solo se informará si el punto tiene unidades configuradas.
  * _his_: marcardor (se indica con el valor `m`) que indica que se trata de un histórico
  * _tz_: zona horaría que se utiliza para los valores del punto.
  * _kind_: tipo de datos. Será uno entre _Number_, _Bool_ y _Str_.
  * _axHistoryId_: ruta del histórico dentro de espacio _History_ de la station. Solo estará presente para históricos
  * _axType_: tipo de componente Niagara.
  * _navName_: nombre del punto.
  * _id_: teóricamente, es el identificador que debemos utilizar para acceder directamente al punto o histórico. En la práctica responde al patrón `r:{id} {dis}`, y debe ser interpretado para poder ser utilizado en la operación _hisRead_.
  * _point_: marcador que indica que se trata de un punto. En el caso de uso que nos ocupa siempre vamos a tratar con puntos que pueden ser, además, históricos.
  * _dis_: en teoría, el resultado de aplicar una macro interna que puede hacer uso de otras propiedades (nombre del punto, nombre del componente padre, etc) para construir un nombre descriptivo del punto. En la práctica, casi siempre es el mismo que _navName.
* _cols_: lista de los campos que pueden aparecer en los documentos.

# 7. Operación **hisRead**
La operación _hisRead_ nos permite leer valores de un histórico para unas fechas determinadas.

## 7.1. Cuerpo de la petición

``` 
ver:"3.0"
id,range
@H.Alvia_J1.AR_GC03_UPS_EnerActTot,"2022-08-01,2022-08-15"
```

### 7.1.1. Obtener id de punto desde respuesta de **read**
* Si hemos obtenido una lista de puntos mediante la operación _read_ y queremos leer el histórico de uno de ellos, veremos que en el campo _id_ del doc correspondiente tenemos un valor que responde al patrón `r:{id} {dis}`. Debemos utilizar el token `{id}` (el valor entre `r:` y el 1er espacio en blanco) precedido de un caracter `@`.

### 7.1.2. Qué valores se pueden indicar en _range_
* "today" : literal para pedir los valores de hoy
* "yesterday": literal para pedir los valores de ayer
* "{date}" : fecha en formato `yyyy-MM-dd`. Pide los datos entre las 00:00:00 del día indicado y las 00:00:00 del día siguiente.
* "{date},{date}" : Pide los datos entre las 00:00:00 de la primera fecha y las 00:00:00 del día posterior a la 2a fecha.
* "{dateTime},{dateTime}" : pide los datos entre dos instantes especificados en formato `yyyy-MM-dd'T'HH:mm:ssZ`
* "{dateTime}" : pide los datos posteriores al instante especificado.

## 7.2. Respuesta OK desde station Niagara
```json
{
  "meta": {
    "ver": "3.0",
    "id": "r:H.Alvia_J1.AR_GC03_UPS_EnerActTot",
    "hisStart": "t:2022-08-01T00:00:00+02:00 Paris",
    "hisEnd": "t:2022-08-16T00:00:00+02:00 Paris"
  },
  "rows": [
    {
      "val": "n:-1441740.875 kWh",
      "ts": "t:2022-08-01T00:00:05.852+02:00 Paris"
    },
    {
      "val": "n:-1441694.125 kWh",
      "ts": "t:2022-08-01T00:15:05.880+02:00 Paris"
    },
    {
      "val": "n:-1441648.75 kWh",
      "ts": "t:2022-08-01T00:30:05.824+02:00 Paris"
    },
    {
      "val": "n:-1441600.625 kWh",
      "ts": "t:2022-08-01T00:45:05.840+02:00 Paris"
    },
    {
      "val": "n:-1438090.875 kWh",
      "ts": "t:2022-08-01T17:30:05.611+02:00 Paris"
    },
    {
      "val": "n:-1438030.375 kWh",
      "ts": "t:2022-08-01T17:45:05.776+02:00 Paris"
    },
    ...
  ],
  "cols": [
    {
      "name": "ts"
    },
    {
      "name": "val"
    }
  ]
}
```
* _meta_ : con los siguientes campos
  * _id_ : el identificador del histórico consultado, con el prefijo `r:`
  * _hisStart_ : fecha de inicio efectiva de la consulta
  * _hisEnd_ : fecha final efectiva de la consulta
* _rows_ : lista de docs con las entradas de la serie temporal.
  * _val_ : valor con prefijo indicando el tipo de datos.
    * `n:` si se trata de un valor numérico
    * `b:` si se trata de un valor booleano
    * `s:` si se trata de un valor alfanumérico
  * _ts_ : marca de tiempo con patrón `'t:'yyyy-MM-dd'T'hh:mm:ssSZZZ`
* _cols_ : lista de las columnas que aparecen en cada doc de la lista de la serie temporal devuelta.

# 8. Gestión de errores
## 8.1. Errores HTTP
Los errores HTTP deben ser gestionados cuando se obtiene la respuesta mediante la librería HTTP que se utilice (la nativa de Java en el ejemplo) .

## 8.2. Errores Haystack
Los errores Haystack se devuelven dentro de una respuesta HTTP con código de estado 200 (OK).
Se puede determinar si una respuesta Haystack es de error examinando el contenido del campo _meta_ y comprobando la existencia del marcador _err_.

Ejemplo de respuesta de error Haystack:
```json
{
  "meta": {
    "ver": "3.0",
    "err": "m:",
    "errTrace": ""
  },
  "rows": [],
  "cols": [
    {
      "name": "empty"
    }
  ]
}
```

No se ha incorporado al ejemplo código que ilustre la gestión de errores.

# 9. Recursos
* Niagara module for Project Haystack: [https://github.com/ci-richard-mcelhinney/nhaystack](https://github.com/ci-richard-mcelhinney/nhaystack)
* Project Haystack website: [https://project-haystack.org/](https://project-haystack.org/)
  * Área de descargas: [https://project-haystack.org/download](https://project-haystack.org/download)
  * HTTP API: [https://project-haystack.org/doc/docHaystack/HttpApi](https://project-haystack.org/doc/docHaystack/HttpApi)
  * Operaciones: [https://project-haystack.org/doc/docHaystack/Ops](https://project-haystack.org/doc/docHaystack/Ops)
  * Autenticación: [https://project-haystack.org/doc/docHaystack/Auth](https://project-haystack.org/doc/docHaystack/Auth)