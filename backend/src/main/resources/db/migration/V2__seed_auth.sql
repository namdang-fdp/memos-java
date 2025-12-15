INSERT INTO permission (id, name, description)
VALUES
  (gen_random_uuid(), 'ACCOUNT.UPDATE_SELF', 'User updates own account'),
  (gen_random_uuid(), 'ACCOUNT.DELETE_SELF', 'User deletes own account'),
  (gen_random_uuid(), 'PROJECT.CREATE', 'User can create project'),
  (gen_random_uuid(), 'VIEW.PUBLIC_CONTENT', 'User views public content'),
  (gen_random_uuid(), 'ADMIN.FULL_ACCESS', 'Full admin access to all resources')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role (id, name, description)
VALUES
  (gen_random_uuid(), 'MEMBER', 'Default user role'),
  (gen_random_uuid(), 'ADMIN', 'System administrator')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.name IN (
  'ACCOUNT.UPDATE_SELF',
  'ACCOUNT.DELETE_SELF',
  'PROJECT.CREATE',
  'VIEW.PUBLIC_CONTENT'
)
WHERE r.name = 'MEMBER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.name IN ('ADMIN.FULL_ACCESS')
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
