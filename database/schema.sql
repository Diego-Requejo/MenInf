-- Codificación de la conexión: necesaria por los acentos y las columnas "contraseña" y "año"
SET NAMES utf8mb4;

DROP TABLE IF EXISTS PreferenciaLenguaje;
DROP TABLE IF EXISTS LenguajesAsignatura;
DROP TABLE IF EXISTS Lenguaje;
DROP TABLE IF EXISTS Cursada;
DROP TABLE IF EXISTS Usuario;
DROP TABLE IF EXISTS AsignaturaPrerrequisito;
DROP TABLE IF EXISTS Periodo;
DROP TABLE IF EXISTS ProfesorAsignatura;
DROP TABLE IF EXISTS Profesor;
DROP TABLE IF EXISTS AsignaturasMencion;
DROP TABLE IF EXISTS Asignatura;


CREATE TABLE Asignatura(
                           cod INTEGER, check ( cod>0 ),
                           nombre VARCHAR(100) NOT NULL ,
                           abreviatura VARCHAR(10) NOT NULL ,
                           cuatri INTEGER NOT NULL ,
                           PRIMARY KEY (cod)
);

CREATE TABLE AsignaturasMencion(
                                   mencion ENUM('IS', 'CO', 'TI', 'PreMencion'),
                                   codA INTEGER,
                                   cursoAcademico INTEGER NOT NULL, CHECK ( cursoAcademico>0), CHECK ( cursoAcademico<=4),
                                   obligatoriedad BOOLEAN NOT NULL,
                                   PRIMARY KEY (mencion, codA),
                                   FOREIGN KEY (codA) REFERENCES Asignatura(cod)

);

CREATE TABLE Profesor(
                         correo VARCHAR(50),
                         nombre VARCHAR(50) NOT NULL,
                         PRIMARY KEY (correo)

);

CREATE TABLE ProfesorAsignatura(
                                   codA INTEGER,
                                   correoP VARCHAR(50),
                                   PRIMARY KEY (codA, correoP),
                                   FOREIGN KEY (codA) REFERENCES Asignatura(cod),
                                   FOREIGN KEY (correoP) REFERENCES Profesor(correo)
);

CREATE TABLE Periodo(
                        idPeriodo VARCHAR(6 ),
    /*Id artificial formado por "[2 últimosdígitos asigantura][grupo][número de día]"
    Ejemplo: "00T11"*/
                        grupo VARCHAR(5) NOT NULL,
    /*Día de 1 al 5 siendo los días de la semana*/
                        dia INTEGER NOT NULL, CHECK (dia>0), CHECK (dia<=5),
                        horaIni INTEGER NOT NULL, CHECK (horaIni>0), CHECK (horaIni<=23),
                        horaFin INTEGER NOT NULL, CHECK (horaFin>0), CHECK (horaFin<=23),
                        codA INTEGER NOT NULL,
                        PRIMARY KEY (idPeriodo),
                        FOREIGN KEY (codA) REFERENCES Asignatura(cod)
);

CREATE TABLE AsignaturaPrerrequisito(
                                        codA INTEGER,
                                        codAPre INTEGER,
                                        PRIMARY KEY (codA, codAPre),
                                        FOREIGN KEY (codA) REFERENCES Asignatura(cod),
                                        FOREIGN KEY (codAPre) REFERENCES Asignatura(cod)
);

CREATE TABLE Usuario(
                        nombre VARCHAR(50),
                        contraseña VARCHAR(50) NOT NULL,
                        biografia VARCHAR(200),
                        mencion ENUM('IS', 'CO', 'TI', 'PreMencion') NOT NULL,
                        tfg BOOLEAN NOT NULL ,
                        practicas BOOLEAN NOT NULL,
                        PRIMARY KEY (nombre)
);

CREATE TABLE Cursada(
                        codA     INTEGER,
                        nomU     VARCHAR(50),
                        estado   ENUM ('superada', 'suspensa'),
                        año INTEGER NOT NULL, check (año>=2015),
                        favorita BOOLEAN                                   NOT NULL,
                        odiada   BOOLEAN                                   NOT NULL,
                        PRIMARY KEY (codA, nomU),
                        FOREIGN KEY (codA) REFERENCES Asignatura(cod),
                        FOREIGN KEY (nomU) REFERENCES Usuario(nombre)
);

CREATE TABLE Lenguaje(
                         nombre VARCHAR(50),
                         PRIMARY KEY (nombre)
);

CREATE TABLE LenguajesAsignatura(
                                    codA INTEGER,
                                    nomL VARCHAR(50),
                                    PRIMARY KEY (codA, nomL),
                                    FOREIGN KEY (codA) REFERENCES Asignatura(cod),
                                    FOREIGN KEY (nomL) REFERENCES Lenguaje(nombre)
);

CREATE TABLE PreferenciaLenguaje(
                                    nomU VARCHAR(50),
                                    nomL VARCHAR(50),
                                    nota INTEGER NOT NULL, CHECK (nota >= 1 AND nota <= 5),
                                    PRIMARY KEY (nomU, nomL),
                                    FOREIGN KEY (nomU) REFERENCES Usuario(nombre),
                                    FOREIGN KEY (nomL) REFERENCES Lenguaje(nombre)

);