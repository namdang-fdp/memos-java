CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO public.role (id, name, description)
VALUES
    (gen_random_uuid(), 'ADMIN',   'Memos Admin'),
    (gen_random_uuid(), 'MEMBER',  'Memos member')
ON CONFLICT (name) DO NOTHING;

