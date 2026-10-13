

\restrict byIqsasIaGmzxDF3GKbQIZWqHodE673eHHFwaWeheNbEWVSqVfcoLz37uOs5PO8



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


CREATE FUNCTION public.check_alternating_doctor_shifts() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    previous_start_time TIME;
BEGIN
    SELECT start_time
    INTO previous_start_time
    FROM doctor_schedule
    WHERE doctor_id = NEW.doctor_id
      AND work_date < NEW.work_date
    ORDER BY work_date DESC
    LIMIT 1;

    IF previous_start_time IS NOT NULL
        AND previous_start_time = NEW.start_time THEN
        RAISE EXCEPTION
            'Смены врача должны чередоваться. Предыдущая смена: %, дата: %',
            previous_start_time,
            NEW.work_date;
    END IF;

    RETURN NEW;
END;
$$;




CREATE FUNCTION public.check_appointment_district() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    patient_district BIGINT;
    doctor_type VARCHAR(20);
    doctor_district BIGINT;
    appointment_date DATE;
    free_slots INTEGER;
BEGIN


    SELECT district_id
    INTO patient_district
    FROM patient
    WHERE id = NEW.patient_id;


    SELECT
        d.type,
        d.district_id,
        ds.work_date
    INTO
        doctor_type,
        doctor_district,
        appointment_date
    FROM doctor_schedule ds
             JOIN doctor d
                  ON d.id = ds.doctor_id
    WHERE ds.id = NEW.schedule_id;


    IF doctor_type = 'SPECIALIST' THEN
        RETURN NEW;
    END IF;


    IF doctor_district = patient_district THEN
        RETURN NEW;
    END IF;


    free_slots := get_district_free_slots(
            patient_district,
            appointment_date
                  );

    IF free_slots > 0 THEN
        RAISE EXCEPTION
            'Запись запрещена: на участке пациента есть % свободных слотов',
            free_slots;
    END IF;

    RETURN NEW;
END;
$$;




CREATE FUNCTION public.check_appointment_time() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    schedule_start TIME;
    schedule_end TIME;
BEGIN
    SELECT start_time, end_time
    INTO schedule_start, schedule_end
    FROM doctor_schedule
    WHERE id = NEW.schedule_id;

    IF NEW.appointment_time < schedule_start
        OR NEW.appointment_time >= schedule_end THEN

        RAISE EXCEPTION
            'Врач не работает в это время: расписание % - %',
            schedule_start,
            schedule_end;
    END IF;

    RETURN NEW;
END;
$$;




CREATE FUNCTION public.get_district_free_slots(p_district_id bigint, p_work_date date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    total_free_slots INTEGER;
BEGIN
    SELECT COALESCE(
                   SUM(
                           (
                               EXTRACT(EPOCH FROM (ds.end_time - ds.start_time)) / 1800
                               )::INTEGER
                               -
                           (
                               SELECT COUNT(*)
                               FROM appointment a
                               WHERE a.schedule_id = ds.id
                           )::INTEGER
                   ),
                   0
           )
    INTO total_free_slots
    FROM doctor d
             JOIN doctor_schedule ds
                  ON ds.doctor_id = d.id
                      AND ds.work_date = p_work_date
    WHERE d.type = 'THERAPIST'
      AND d.district_id = p_district_id;

    RETURN total_free_slots;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;



CREATE TABLE public.appointment (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    patient_id bigint NOT NULL,
    appointment_time time without time zone NOT NULL,
    CONSTRAINT check_appointment_minutes CHECK ((EXTRACT(minute FROM appointment_time) = ANY (ARRAY[(0)::numeric, (30)::numeric]))),
    CONSTRAINT check_appointment_time CHECK (((appointment_time >= '08:00:00'::time without time zone) AND (appointment_time < '17:00:00'::time without time zone)))
);



CREATE SEQUENCE public.appointment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.appointment_id_seq OWNED BY public.appointment.id;




CREATE TABLE public.district (
    id bigint NOT NULL,
    number integer NOT NULL,
    address character varying(255) NOT NULL
);




CREATE SEQUENCE public.district_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.district_id_seq OWNED BY public.district.id;




CREATE TABLE public.doctor (
    id bigint NOT NULL,
    full_name character varying(255) NOT NULL,
    type character varying(20) NOT NULL,
    district_id bigint,
    CONSTRAINT check_doctor_district CHECK (((((type)::text = 'THERAPIST'::text) AND (district_id IS NOT NULL)) OR (((type)::text = 'SPECIALIST'::text) AND (district_id IS NULL)))),
    CONSTRAINT check_doctor_type CHECK (((type)::text = ANY ((ARRAY['THERAPIST'::character varying, 'SPECIALIST'::character varying])::text[])))
);




CREATE SEQUENCE public.doctor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.doctor_id_seq OWNED BY public.doctor.id;




CREATE TABLE public.doctor_schedule (
    id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    work_date date NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    CONSTRAINT check_schedule_shift CHECK ((((start_time = '08:00:00'::time without time zone) AND (end_time = '12:00:00'::time without time zone)) OR ((start_time = '13:00:00'::time without time zone) AND (end_time = '17:00:00'::time without time zone)))),
    CONSTRAINT check_schedule_time CHECK ((end_time > start_time)),
    CONSTRAINT check_schedule_weekday CHECK (((EXTRACT(isodow FROM work_date) >= (1)::numeric) AND (EXTRACT(isodow FROM work_date) <= (5)::numeric)))
);




CREATE SEQUENCE public.doctor_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.doctor_schedule_id_seq OWNED BY public.doctor_schedule.id;



CREATE TABLE public.patient (
    id bigint NOT NULL,
    full_name character varying(255) NOT NULL,
    birth_date date NOT NULL,
    address character varying(255) NOT NULL,
    district_id bigint NOT NULL
);




CREATE SEQUENCE public.patient_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.patient_id_seq OWNED BY public.patient.id;




ALTER TABLE ONLY public.appointment ALTER COLUMN id SET DEFAULT nextval('public.appointment_id_seq'::regclass);




ALTER TABLE ONLY public.district ALTER COLUMN id SET DEFAULT nextval('public.district_id_seq'::regclass);




ALTER TABLE ONLY public.doctor ALTER COLUMN id SET DEFAULT nextval('public.doctor_id_seq'::regclass);




ALTER TABLE ONLY public.doctor_schedule ALTER COLUMN id SET DEFAULT nextval('public.doctor_schedule_id_seq'::regclass);




ALTER TABLE ONLY public.patient ALTER COLUMN id SET DEFAULT nextval('public.patient_id_seq'::regclass);




ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT appointment_pkey PRIMARY KEY (id);




ALTER TABLE ONLY public.district
    ADD CONSTRAINT district_number_key UNIQUE (number);




ALTER TABLE ONLY public.district
    ADD CONSTRAINT district_pkey PRIMARY KEY (id);




ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_pkey PRIMARY KEY (id);




ALTER TABLE ONLY public.doctor_schedule
    ADD CONSTRAINT doctor_schedule_pkey PRIMARY KEY (id);




ALTER TABLE ONLY public.patient
    ADD CONSTRAINT patient_pkey PRIMARY KEY (id);




ALTER TABLE ONLY public.doctor_schedule
    ADD CONSTRAINT uq_doctor_schedule_date UNIQUE (doctor_id, work_date);




ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT uq_patient_appointment UNIQUE (schedule_id, patient_id, appointment_time);




ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT uq_schedule_appointment_time UNIQUE (schedule_id, appointment_time);




CREATE TRIGGER trg_check_alternating_doctor_shifts BEFORE INSERT OR UPDATE OF doctor_id, work_date, start_time ON public.doctor_schedule FOR EACH ROW EXECUTE FUNCTION public.check_alternating_doctor_shifts();




CREATE TRIGGER trg_check_appointment_district BEFORE INSERT OR UPDATE ON public.appointment FOR EACH ROW EXECUTE FUNCTION public.check_appointment_district();




CREATE TRIGGER trg_check_appointment_time BEFORE INSERT OR UPDATE ON public.appointment FOR EACH ROW EXECUTE FUNCTION public.check_appointment_time();




ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES public.patient(id) ON DELETE RESTRICT;




ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT fk_appointment_schedule FOREIGN KEY (schedule_id) REFERENCES public.doctor_schedule(id) ON DELETE CASCADE;




ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT fk_doctor_district FOREIGN KEY (district_id) REFERENCES public.district(id) ON DELETE RESTRICT;




ALTER TABLE ONLY public.patient
    ADD CONSTRAINT fk_patient_district FOREIGN KEY (district_id) REFERENCES public.district(id) ON DELETE RESTRICT;




ALTER TABLE ONLY public.doctor_schedule
    ADD CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES public.doctor(id) ON DELETE CASCADE;




\unrestrict byIqsasIaGmzxDF3GKbQIZWqHodE673eHHFwaWeheNbEWVSqVfcoLz37uOs5PO8

