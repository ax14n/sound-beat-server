--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4 (Debian 17.4-1.pgdg120+2)
-- Dumped by pg_dump version 17.4

-- Started on 2025-06-12 10:34:03 UTC

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3431 (class 1262 OID 16384)
-- Name: streaming_db; Type: DATABASE; Schema: -; Owner: admin
--

CREATE DATABASE streaming_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE streaming_db OWNER TO admin;

\connect streaming_db

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 228 (class 1255 OID 16482)
-- Name: set_default_username(); Type: FUNCTION; Schema: public; Owner: admin
--

CREATE FUNCTION public.set_default_username() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NEW.username IS NULL THEN
    NEW.username := 'user' || nextval('username_seq');
  END IF;
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.set_default_username() OWNER TO admin;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 224 (class 1259 OID 16441)
-- Name: favorites; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.favorites (
    user_id integer NOT NULL,
    song_id integer NOT NULL
);


ALTER TABLE public.favorites OWNER TO admin;

--
-- TOC entry 226 (class 1259 OID 16457)
-- Name: history; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.history (
    history_id integer NOT NULL,
    user_id integer NOT NULL,
    song_id integer NOT NULL,
    played_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.history OWNER TO admin;

--
-- TOC entry 225 (class 1259 OID 16456)
-- Name: history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.history_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.history_history_id_seq OWNER TO admin;

--
-- TOC entry 3432 (class 0 OID 0)
-- Dependencies: 225
-- Name: history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.history_history_id_seq OWNED BY public.history.history_id;


--
-- TOC entry 223 (class 1259 OID 16426)
-- Name: playlist_songs; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.playlist_songs (
    playlist_id integer NOT NULL,
    song_id integer NOT NULL
);


ALTER TABLE public.playlist_songs OWNER TO admin;

--
-- TOC entry 222 (class 1259 OID 16414)
-- Name: playlists; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.playlists (
    playlist_id integer NOT NULL,
    name character varying(255) NOT NULL,
    user_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.playlists OWNER TO admin;

--
-- TOC entry 221 (class 1259 OID 16413)
-- Name: playlists_playlist_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.playlists_playlist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.playlists_playlist_id_seq OWNER TO admin;

--
-- TOC entry 3433 (class 0 OID 0)
-- Dependencies: 221
-- Name: playlists_playlist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.playlists_playlist_id_seq OWNED BY public.playlists.playlist_id;


--
-- TOC entry 220 (class 1259 OID 16404)
-- Name: songs; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.songs (
    song_id integer NOT NULL,
    title character varying(255) NOT NULL,
    artist character varying(255) NOT NULL,
    duration integer NOT NULL,
    url character varying(500) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    genres text[]
);


ALTER TABLE public.songs OWNER TO admin;

--
-- TOC entry 219 (class 1259 OID 16403)
-- Name: songs_song_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.songs_song_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.songs_song_id_seq OWNER TO admin;

--
-- TOC entry 3434 (class 0 OID 0)
-- Dependencies: 219
-- Name: songs_song_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.songs_song_id_seq OWNED BY public.songs.song_id;


--
-- TOC entry 227 (class 1259 OID 16481)
-- Name: username_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.username_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.username_seq OWNER TO admin;

--
-- TOC entry 218 (class 1259 OID 16390)
-- Name: users; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    username text NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    date_joined timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    image_url character varying(255) DEFAULT '/home/me/Documentos/imagenes-usuarios/Screenshot_20250126_173807.png'::character varying
);


ALTER TABLE public.users OWNER TO admin;

--
-- TOC entry 217 (class 1259 OID 16389)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO admin;

--
-- TOC entry 3435 (class 0 OID 0)
-- Dependencies: 217
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 3242 (class 2604 OID 16460)
-- Name: history history_id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.history ALTER COLUMN history_id SET DEFAULT nextval('public.history_history_id_seq'::regclass);


--
-- TOC entry 3240 (class 2604 OID 16417)
-- Name: playlists playlist_id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlists ALTER COLUMN playlist_id SET DEFAULT nextval('public.playlists_playlist_id_seq'::regclass);


--
-- TOC entry 3238 (class 2604 OID 16407)
-- Name: songs song_id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.songs ALTER COLUMN song_id SET DEFAULT nextval('public.songs_song_id_seq'::regclass);


--
-- TOC entry 3235 (class 2604 OID 16393)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 3259 (class 2606 OID 16445)
-- Name: favorites favorites_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.favorites
    ADD CONSTRAINT favorites_pkey PRIMARY KEY (user_id, song_id);


--
-- TOC entry 3245 (class 2606 OID 16480)
-- Name: users fk_username; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_username UNIQUE (username);


--
-- TOC entry 3261 (class 2606 OID 16463)
-- Name: history history_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_pkey PRIMARY KEY (history_id);


--
-- TOC entry 3257 (class 2606 OID 16430)
-- Name: playlist_songs playlist_songs_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlist_songs
    ADD CONSTRAINT playlist_songs_pkey PRIMARY KEY (playlist_id, song_id);


--
-- TOC entry 3253 (class 2606 OID 16420)
-- Name: playlists playlists_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlists
    ADD CONSTRAINT playlists_pkey PRIMARY KEY (playlist_id);


--
-- TOC entry 3251 (class 2606 OID 16412)
-- Name: songs songs_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.songs
    ADD CONSTRAINT songs_pkey PRIMARY KEY (song_id);


--
-- TOC entry 3255 (class 2606 OID 16478)
-- Name: playlists unique_playlist_name_per_user; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlists
    ADD CONSTRAINT unique_playlist_name_per_user UNIQUE (name, user_id);


--
-- TOC entry 3247 (class 2606 OID 16402)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3249 (class 2606 OID 16398)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3269 (class 2620 OID 16483)
-- Name: users set_username_before_insert; Type: TRIGGER; Schema: public; Owner: admin
--

CREATE TRIGGER set_username_before_insert BEFORE INSERT ON public.users FOR EACH ROW EXECUTE FUNCTION public.set_default_username();


--
-- TOC entry 3265 (class 2606 OID 16451)
-- Name: favorites favorites_song_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.favorites
    ADD CONSTRAINT favorites_song_id_fkey FOREIGN KEY (song_id) REFERENCES public.songs(song_id) ON DELETE CASCADE;


--
-- TOC entry 3266 (class 2606 OID 16446)
-- Name: favorites favorites_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.favorites
    ADD CONSTRAINT favorites_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- TOC entry 3267 (class 2606 OID 16469)
-- Name: history history_song_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_song_id_fkey FOREIGN KEY (song_id) REFERENCES public.songs(song_id) ON DELETE CASCADE;


--
-- TOC entry 3268 (class 2606 OID 16464)
-- Name: history history_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- TOC entry 3263 (class 2606 OID 16431)
-- Name: playlist_songs playlist_songs_playlist_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlist_songs
    ADD CONSTRAINT playlist_songs_playlist_id_fkey FOREIGN KEY (playlist_id) REFERENCES public.playlists(playlist_id) ON DELETE CASCADE;


--
-- TOC entry 3264 (class 2606 OID 16436)
-- Name: playlist_songs playlist_songs_song_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlist_songs
    ADD CONSTRAINT playlist_songs_song_id_fkey FOREIGN KEY (song_id) REFERENCES public.songs(song_id) ON DELETE CASCADE;


--
-- TOC entry 3262 (class 2606 OID 16421)
-- Name: playlists playlists_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.playlists
    ADD CONSTRAINT playlists_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


-- Completed on 2025-06-12 10:34:03 UTC

--
-- PostgreSQL database dump complete
--

