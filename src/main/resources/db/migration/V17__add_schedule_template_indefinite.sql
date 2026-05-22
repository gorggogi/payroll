-- Add indefinite flag to weekly_schedule_template.
-- TRUE  = carry over to future months until explicitly changed (default, matches previous behaviour).
-- FALSE = applies to that specific month only; never used as a carryover source.
ALTER TABLE weekly_schedule_template
    ADD COLUMN indefinite BOOLEAN NOT NULL DEFAULT TRUE;
